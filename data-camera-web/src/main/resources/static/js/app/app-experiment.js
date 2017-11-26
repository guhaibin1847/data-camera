/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/11/11
 *  Description:
 */

var chart_object = {};
var exp_monitor_interval = {};
var exp_newest_timestamp = {};
var recorder_timestamp = {};

function initResourceOfExperimentPage() {
    var $loader = $("#app-experiment-loading");
    $loader.fakeLoader({
        timeToHide: 10000,
        spinner:"spinner4",
        bgColor:"rgba(154, 154, 154, 0.7)"
    });

    // init experiment track and sensors
    for (var exp_id in experiments){
        var experiment = experiments[exp_id];
        exp_newest_timestamp[exp_id] = new Date().getTime();

        for (var i in experiment['trackInfoList']){
            var track = experiment['trackInfoList'][i];
            var track_id = track['id'];

            // init track chart
            var legend = (null == track['sensor']) ? [] : track['sensor']['sensorConfig']['dimension'].split(';');
            var chart_dom = "experiment-track-" + exp_id + "-" + track_id;
            var chart = echarts.init(document.getElementById(chart_dom), "", opts = {});
            chart.setOption(experimentChartOption(legend));
            chart_object[chart_dom] = chart;

            // init track bound sensor
            var $track_bound_dom = $('#track-bound-' + track_id);
            var sensor = (null == track['sensor'])?null:track['sensor'];
            var source = [];
            var value = "";
            if (null != sensor){
                // already bound
                source.push({
                    value: sensor.id,
                    text: sensor.name
                });
                value = sensor.id;
            } else {
                for (var index in freeSensors){
                    if (track['type'] == freeSensors[index]['sensorConfig']['type']) {
                        source.push({
                            value: freeSensors[index].id,
                            text: freeSensors[index].name
                        });
                    }
                }
            }
            $track_bound_dom.editable({
                prepend: '不绑定设备',
                source: source,
                value: value,
                sourceError: 'error loading data',
                pk: track_id,
                validate: function (value) {
                    var track_id = $(this)['context']['id'].split('-')[2];
                    for (var current_id in tracks){
                        if (current_id + '' != track_id){
                            continue;
                        }
                        var e = tracks[current_id]['experiment']['id'];
                        if (isExperimentMonitor[e] == 1){
                            return '数据监控中，不能进行绑定操作';
                        }
                    }
                },
                url: crud_address + '/bound/toggle',
                success: function(result) {
                    window.location.href = current_address + "?id=" + app['id'];
                },
                error: function (error) {
                    message_info('绑定操作失败: ' + error, 'error');
                }
            });
        }
    }

    // init experiment which is in monitoring state
    for (var id in isExperimentMonitor){
        var exp_monitor_btn = $('#experiment-monitor-' + id);
        var exp_monitor_dom = $('#experiment-es-' + id);
        var exp_recorder_btn = $('#experiment-recorder-' + id);
        var exp_recorder_dom = $('#experiment-rs-' + id);

        if (isExperimentMonitor[id] == 1){
            exp_monitor_dom.removeClass('label-warning').addClass('label-success').text('正在监控');
            exp_monitor_btn.html('停止监控');

            if (isExperimentRecorder[id] == 1){
                exp_recorder_dom.removeClass('label-warning').addClass('label-success').text('正在录制');
                exp_recorder_btn.html("停止录制");

                recorder_timestamp[id] = [];
                recorder_timestamp[id].push(expRecorderTime[id]);

                exp_newest_timestamp[id] = new Date(parseTime(expRecorderTime[id])).getTime();
            }
            doInterval(id);
        }
    }

    $loader.fadeOut();
}

function expMonitor(button){
    var exp_id = button.getAttribute('data');
    var exp_state_dom = $('#experiment-es-' + exp_id);
    var exp_monitor_btn = $('#experiment-monitor-' + exp_id);

    if (!boundSensors.hasOwnProperty(exp_id)){
        message_info('实验未绑定任何设备', 'error');
        return;
    }

    $.ajax({
        type: 'get',
        url: crud_address + "/monitor",
        data: {
            "exp-id": exp_id,
            "action": isExperimentMonitor[exp_id]
        },
        success: function (response) {
            if (response == -1 || response == 0){
                message_info('操作无效', 'error');
                return;
            }
            if (isExperimentMonitor[exp_id] == 0){
                // start monitor
                isExperimentMonitor[exp_id] = 1;
                exp_state_dom.removeClass('label-warning').addClass('label-success').text('正在监控');
                exp_monitor_btn.html('停止监控');

                doInterval(exp_id);
            } else if (isExperimentMonitor[exp_id] == 1){
                // stop monitor
                pageStopMonitor(exp_id);
            }
        },
        error: function (response) {
            message_info("操作失败，失败原因为：" + response, 'error');
        }
    });
}

function expRecorder(button) {
    var exp_id = button.getAttribute('data');
    var exp_state_dom = $('#experiment-rs-' + exp_id);
    var exp_recorder_btn = $('#experiment-recorder-' + exp_id);
    
    if (!isExperimentMonitor.hasOwnProperty(exp_id) || isExperimentMonitor[exp_id] == 0){
        message_info("实验" + exp_id + "未开始监控，无法记录！");
        return;
    }

    $.ajax({
        type: 'get',
        url: crud_address + "/recorder",
        data: {
            "exp-id": exp_id,
            "action": isExperimentRecorder[exp_id]
        },
        success: function (response) {
            if (response == -1 || response == 0){
                message_info('操作无效', 'error');
                return;
            }

            if (!recorder_timestamp.hasOwnProperty(exp_id)){
                recorder_timestamp[exp_id] = [];
            }

            if (recorder_timestamp[exp_id].length % 2 == 0){
                message_info("实验" + exp_id + ": 开始记录");
                exp_state_dom.removeClass('label-warning').addClass('label-success').text('正在录制');
                exp_recorder_btn.html("停止录制");

                isExperimentRecorder[exp_id] = 1;
                recorder_timestamp[exp_id].push(new Date().Format("yyyy-MM-dd HH:mm:ss"));
            } else {
                message_info("实验" + exp_id + ": 停止记录");
                pageStopRecorder(exp_id);
            }
        },
        error: function (response) {
            message_info("操作失败，失败原因为：" + response, 'error');
        }
    });
}

function pageStopMonitor(exp_id){
    if (isExperimentRecorder[exp_id] == 1){
        pageStopRecorder(exp_id);
    }

    isExperimentMonitor[exp_id] = 0;
    $('#experiment-es-' + exp_id).removeClass('label-success').addClass('label-warning').text('非监控');
    $('#experiment-monitor-' + exp_id).html('开始监控');

    clearInterval(exp_monitor_interval[exp_id]);
    delete exp_monitor_interval[exp_id];
}

function pageStopRecorder(exp_id){
    $('#experiment-rs-' + exp_id).removeClass('label-success').addClass('label-warning').text('非录制');
    $('#experiment-recorder-' + exp_id).html("开始录制");
    isExperimentRecorder[exp_id] = 0;
    recorder_timestamp[exp_id].push(new Date().Format("yyyy-MM-dd HH:mm:ss"));
}

function doInterval(exp_id){
    exp_monitor_interval[exp_id] = setInterval(function(){
        askForData(exp_id);
    }, 2000);

    function askForData(exp_id) {
        var exp_bound_sensors = boundSensors[exp_id];
        $.get(data_addrss + "/monitor", {
            "exp-id": exp_id,
            "timestamp": exp_newest_timestamp[exp_id]
        }, function (response) {
            if (isEmptyObject(response)){
                return;
            }
            // --- traverse the sensors of this experiment
            for (var index in exp_bound_sensors){
                var sensor = exp_bound_sensors[index];
                if (!response.hasOwnProperty(sensor['id'])){
                    continue;
                }
                // --- init
                var sensor_id = sensor['id'];
                var track_id = sensor['trackId'];
                var chart_dom = "experiment-track-" + exp_id + "-" + track_id;
                var chart = chart_object[chart_dom];
                var series = chart.getOption()['series'];

                // --- update series data
                var legend = sensor['sensorConfig']['dimension'].split(';');
                for (var i in legend){
                    var key = legend[i];
                    if (!response[sensor_id].hasOwnProperty(key)){
                        continue;
                    }
                    var new_data = response[sensor_id][key];
                    series[i]['data'].push.apply( series[i]['data'], new_data );

                    var new_time = Date.parse(new_data[new_data.length - 1]['value'][0]);
                    if (new_time > exp_newest_timestamp[exp_id]){
                        exp_newest_timestamp[exp_id] = new_time;
                    }
                }

                // --- update series markArea (if recorder)
                if (recorder_timestamp.hasOwnProperty(exp_id)){
                    var mark_list = series[0]['markArea']['data'];
                    var recorder_length = recorder_timestamp[exp_id].length;
                    if (recorder_length % 2 == 1){
                        // - recorder ing
                        var mark_index = Math.floor(recorder_length/2);
                        mark_list[mark_index] = [{
                            xAxis: parseTime(recorder_timestamp[exp_id][recorder_length - 1])
                        }, {
                            xAxis: new Date().Format("yyyy-MM-dd HH:mm:ss")
                        }];
                        series[0]['markArea']['data'] = mark_list;
                    }
                }

                // -- set new option
                chart_object[chart_dom].setOption({
                    series: series
                });
            }
        });
    }
}
