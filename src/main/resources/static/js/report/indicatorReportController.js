Array.prototype.pushArray = function (arr) {
    this.push.apply(this, arr);
};
App.controller('IndicatorReportCtrl', function ($scope, $rootScope, $filter, $http, $sce, $timeout, $stateParams, $location, $window, $interval, uiGridConstants,
                                                $mdDialog, $mdSidenav,
                                                $localStorage, uiGridGroupingConstants, Notification) {

    $scope.type = $stateParams.type;
    $scope.page = $stateParams.page;
    if (!navigator.appVersion.match(/chrome/i)) {
        alert("WBI only supports Chrome. Please use it when accessing WBI");
    }

    $scope.infos = {};
    $scope.infos.data = [];
    $scope.infos.column = [];

    $scope.pramilestones = [];

    $scope.go = function (url) {
        $location.path(url);
    };
    $scope.openSideMenu = function () {
        $mdSidenav('left').toggle()
    };
    $scope.infoGridOptions = {
        data: [],
        enableGridMenu: true,
        exporterMenuPdf: false,
        exporterCsvFilename: 'project_information.csv',
        enableFiltering: true,
        columnDefs: [],
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
        },
        minRowsToShow: 23
    };

    $scope.praGridOptions = {
        data: $scope.pramilestones,
        enableGridMenu: true,
        exporterMenuPdf: false,
        enableColumnResizing: true,
        exporterCsvFilename: 'project_pra.csv',
        enableFiltering: true,
        columnDefs: [
            {
                field: 'chip',
                name: 'chip',
                cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
            },
            {
                field: 'revision',
                name: 'rev',
                cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
            },
            {
                field: 'process node',
                name: 'process node',
                cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
            },
            {field: 'to', name: 'to', cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"},
            {
                field: 'to month',
                name: 'to month',
                cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
            },
            {
                field: 'to quarter',
                name: 'to quarter',
                cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
            },
            {
                field: 'to year',
                name: 'to Year',
                cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
            },
            {
                field: 'pra',
                name: 'pra',
                cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
            },
            {
                field: 'pra month',
                name: 'pra month',
                cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
            },
            {
                field: 'pra quarter',
                name: 'pra quarter',
                cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
            },
            {
                field: 'pra year',
                name: 'pra year',
                cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
            },
            {
                field: 'pra target',
                name: 'pra target',
                cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
            }
        ],
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
        },
        minRowsToShow: 23
    };

    $scope.sendIndicatorReportEmail = function () {
        Notification.info({
            message: 'Please Wait!! Generating email....',
            delay: 10000,
            replaceMessage: true,
            positionY: 'top'
        });
        var content = "<html><body><style>body{font-family: Calibri, san-serif;} a {text-decoration: none !important;color:#000000;} " +
            "table {border-spacing:0px!important} " +
            "</style>" +

            document.getElementById('internalMilestoneTable').innerHTML + "<br>" +
            document.getElementById('ipMilestoneTable').innerHTML + "<br>" +
            document.getElementById('customerMilestoneTable').innerHTML + "<br>";

        $timeout(function () {
            $http.post("/api/report/sendEmailReport", {data: content})
                .then(function (ret) {
                    if (ret.type.match(/error|warning/i)) {
                        Notification.error({
                            message: ret.message,
                            title: ret.title,
                            delay: 10000,
                            replaceMessage: true,
                            positionY: 'top'
                        });
                    }
                    else {
                        Notification.then({
                            message: ret.message,
                            title: ret.title,
                            delay: 10000,
                            replaceMessage: true,
                            positionY: 'top'
                        });
                    }
                });
        }, 10)
    };

    $scope.exportDataToExcel = function (id, title, fname) {
        var uri = 'data:application/vnd.ms-excel;base64,'
            ,
            template = '<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40"><head><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet><x:Name>{worksheet}</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]--></head><body><table>{table}</table></body></html>'
            , base64 = function (s) {
                return window.btoa(unescape(encodeURIComponent(s)))
            }
            , format = function (s, c) {
                return s.replace(/{(\w+)}/g, function (m, p) {
                    return c[p];
                })
            };

        var table = document.getElementById(id);
        var ctx = {worksheet: name || title, table: table.innerHTML};
        var url = uri + base64(format(template, ctx));
        var a = document.createElement('a');
        a.href = url;
        a.download = fname;
        a.click();
    };

    $scope.exportHtmlDataToExcel = function (html, title, fname) {
        var uri = 'data:application/vnd.ms-excel;base64,'
            ,
            template = '<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40"><head><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet><x:Name>{worksheet}</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]--></head><body><table>{table}</table></body></html>'
            , base64 = function (s) {
                return window.btoa(unescape(encodeURIComponent(s)))
            }
            , format = function (s, c) {
                return s.replace(/{(\w+)}/g, function (m, p) {
                    return c[p];
                })
            };

        var ctx = {worksheet: name || title, table: html};
        var url = uri + base64(format(template, ctx));
        var a = document.createElement('a');
        a.href = url;
        a.download = fname;
        a.click();
    };
    var orderBy = $filter('orderBy');
    $scope.trustAsHtml = $sce.trustAsHtml;
    $scope.headlines = [];
    $scope.emailHeadlines = [];
    $scope.alerts = [];
    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };
    $scope.ms = {};
    $scope.ms.viewNote = false;
    $scope.ms.projectStatus = true;

    if ($scope.type.match(/headline/i)) {
        $http.get("/api/report/" + $scope.type + "/" + $scope.page + "/collect")
            .then(function (ret) {
                $scope.headlines = ret.data;
                if ($scope.page.match(/internal/i))
                    $scope.generateInternalHeadlineRow(ret.data);
                else if ($scope.page.match(/customer/i))
                    $scope.generateCustomerHeadlineRow(ret.data);
                else if ($scope.page.match(/ip/i))
                    $scope.generateIPHeadlineRow(ret.data);
            });

        $scope.generateInternalHeadlineRow = function (ret) {
            $scope.internalheadlinerows = [];
            angular.forEach(ret, function (arr, segment) {
                var row = "<th colspan='5' style='background-color:#C0C0C0; text-align:center; font-size:14px; padding:1px'>" + segment.split(/\_/)[1] + "</th>";
                $scope.internalheadlinerows.push(row);
                row = '';
                angular.forEach(arr, function (arr2, idx) {
                    row += "<td  style='width:1%!important;' class='" + arr2.schedule_flag + "'></td>";
                    if (arr2.headProgram) {
                        row += "<td  style='width: 7%;' class='lightgrey'>" + arr2.reduced_pm + "</td>";
                        row += "<td  style='width: 13%;' class='bold uppercase lightgrey'>" + arr2.reportName + "</td>";
                        row += "<td  style='width: 14%;' class='lightgrey'>" + arr2.milestone + "</td>";
                        if (segment.match(/software/i)) {
                            row += "<td  style='width: 70%;' class='lightgrey'><p><i>" + arr2.hlts1 + "</i></p><pre>" + arr2.headline + "</pre></td>";
                        }
                        else {
                            row += "<td  style='width: 70%;' class='lightgrey'><p><i>" + arr2.hlts1 + "</i></p>" + arr2.headline + "</td>";
                        }
                    }
                    else {
                        row += "<td  style='width: 7%;'>" + arr2.reduced_pm + "</td>";
                        row += "<td  style='width: 13%;'>" + arr2.reportName + "</td>";
                        row += "<td  style='width: 14%;'>" + arr2.milestone + "</td>";
                        if (segment.match(/software/i)) {
                            row += "<td  style='width: 70%;'><p><i>" + arr2.hlts1 + "</i></p><pre>" + arr2.headline + "</pre></td>";
                        }
                        else {
                            row += "<td  style='width: 70%;'><p><i>" + arr2.hlts1 + "</i></p>" + arr2.headline + "</td>";
                        }
                    }
                    $scope.internalheadlinerows.push(row);
                    row = '';
                });
            });
        };

        $scope.generateCustomerHeadlineRow = function (ret) {
            $scope.customerheadlinerows = [];
            angular.forEach(ret, function (arr, segment) {
                var sortarr = $filter('orderBy')(arr, 'order');
                var row = {};
                angular.forEach(sortarr, function (arr2, idx) {
                    if (arr2 != null) {
                        row.prediction_flag = arr2.prediction_flag;
                        row.reduce_pm = arr2.reduced_pm;
                        row.base = arr2.base;
                        row.program = arr2.program;
                        row.switch_chip = arr2.switch_chip;
                        row.rid = arr2.rid;
                        row.fcs = '';
                        row.sdk_fcs = '';
                        if (arr2.fcs.match(/[0-9]/)) {
                            row.fcs = "<pre> FCS : " + arr2.fcs + "</pre>";
                        }
                        if (arr2.sdk_fcs.match(/[0-9a-zA-Z]/)) {
                            row.sdk_fcs = "<pre> SDK : " + arr2.sdk_fcs + "</pre>";
                        }
                        row.headline = "<p><i>" + arr2.hlts1 + "</i></p>" + arr2.headline;
                        row.order = arr2.order;
                        $scope.customerheadlinerows.push(row);
                    }
                    row = {};
                });
            });
        };

        $scope.generateIPHeadlineRow = function (ret) {
            $scope.ipheadlinerows = [];
            angular.forEach(ret, function (arr, segment) {
                var row = "<th colspan='5' style='background-color:#C0C0C0; text-align:center; font-size:14px; padding:1px'>" + segment + "</th>";
                $scope.ipheadlinerows.push(row);
                row = '';
                angular.forEach(arr, function (arr2, idx) {
                    row += "<td  style='width:1%!important;' class='" + arr2.schedule_flag + "'></td>";
                    if (arr2.headProgram) {
                        row += "<td  style='width: 10%;' class='lightgrey'>" + arr2.reduced_pm + "</td>";
                        row += "<td  style='width: 10%;' class='lightgrey'>" + arr2.stage + "</td>";
                        row += "<td  style='width: 15%;' class='bold uppercase lightgrey'>" + arr2.reportName + "</td>";
                        if (segment.match(/software/i)) {
                            row += "<td  style='width: 69%;' class='lightgrey'><p><i>" + arr2.hlts1 + "</i></p><pre>" + arr2.headline + "</pre></td>";
                        }
                        else {
                            row += "<td  style='width: 69%;' class='lightgrey'><p><i>" + arr2.hlts1 + "</i></p>" + arr2.headline + "</td>";
                        }
                    }
                    else {
                        row += "<td  style='width: 10%;'>" + arr2.reduced_pm + "</td>";
                        row += "<td  style='width: 15%;'>" + arr2.reportName + "</td>";
                        row += "<td  style='width: 10%;' class='lightgrey'>" + arr2.stage + "</td>";
                        if (segment.match(/software/i)) {
                            row += "<td  style='width: 69%;'><p><i>" + arr2.hlts1 + "</i></p><pre>" + arr2.headline + "</pre></td>";
                        }
                        else {
                            row += "<td  style='width: 69%;'><p><i>" + arr2.hlts1 + "</i></p>" + arr2.headline + "</td>";
                        }
                    }
                    $scope.ipheadlinerows.push(row);
                    row = '';
                });
            });


        };

        $scope.refreshHeadlineReport = function () {
            $http.get("/api/report/" + $scope.type + "/" + $scope.page + "/collect?reload=1")
                .then(function (ret) {
                    $scope.headlines = ret.data;
                });
        };

        $scope.saveHeadlineReportTable = function () {
            var completeRequest = 0;
            $scope.headlineReportLoaded = false;
            $http.post('/api/revision/saveOrder', {'data': $scope.customerheadlinerows})
                .then(function (result) {
                    Notification.then({message: result.data, delay: 1000});
                }).error(function (ret) {
                Notification.error({message: ret.data, delay: 5000, title: ret.code});
            });
        };

        function convertNumberToString(n) {
            if (n < 10)
                return "00" + n;
            else if (n < 100)
                return "0" + n;
            return n;
        }

        $scope.convertToPPT = function () {
            Notification.info({
                message: 'Please Wait!! Generating Report....',
                delay: 5000,
                replaceMessage: true,
                positionY: 'top'
            });
            var obj = {};
            angular.forEach($scope.headlines, function (headlines, segmentIndex) {
                angular.forEach(headlines, function (arr, index) {
                    var re = /\d{2}\/\d{2}\/\d{2,4}/;
                    var hlts = re.exec(arr.hlts1);
                    var diff = Math.round((new Date() - new Date(hlts)) / (1000 * 60 * 60 * 24));
                    if ($scope.page.match(/customer/i) || (diff < 8 && !$scope.page.match(/customer/i))) {
                        var hl = {};
                        if ($scope.page.match(/customer/i)) {
                            hl.status = arr.prediction_flag;
                            hl.fcs = "";
                            var darr = arr.fcs.match(/\d{2}\/\d{2}\/\d{2}/);
                            if (darr && darr.length > 0) {
                                hl.fcs = darr[0];
                            }
                            hl.sdk_fcs = arr.sdk_fcs.replace(/\<br\>/ig, ",").replace(/<(?:.|\n)*?>/gm, ' ');
                        }
                        else {
                            hl.status = arr.schedule_flag;
                        }

                        if (typeof arr.reduced_pm != 'undefined') {
                            hl.pm = arr.reduced_pm.replace(/\&nbsp;/ig, "").replace(/\<br\>/ig, " ");
                        }

                        hl.headline = formatHeadlineIssue(arr.headline);
                        if (typeof arr.switch_chip != 'undefined') {
                            hl.chip = arr.switch_chip.replace(/\<br\>/ig, ",").replace(/<(?:.|\n)*?>/gm, ' ');
                        }

                        if ($scope.page.match(/(chip|internal)/i)) {
                            hl.milestone = formatMilestone(arr.milestone);
                            if (arr.reportName.match(/<ul>/i)) {
                                var ele = angular.element("<div>" + arr.reportName + "</div>");
                                var uls = ele.find("ul");
                                var lis = angular.element(uls[0]).find("li");
                                angular.forEach(lis, function (li, idx) {
                                    var txt = angular.element(li).text();
                                    if (txt.match(/ea:/i)) {
                                        hl.ea = txt;
                                    }
                                    else if (txt.match(/ga:/i)) {
                                        hl.ga = txt;
                                    }
                                });
                            }
                            if (arr.category.toLowerCase().match(/software/i)) {
                                if (arr.base.match(/[0-9a-zA-Z]/) && arr.revision.toLowerCase().indexOf(arr.base.toLowerCase()) == -1) {
                                    obj[segmentIndex.split('_')[0] + convertNumberToString(arr.order) + "&&" +
                                    arr.base.toUpperCase().replace(/program/i, "") + " " + arr.revision.replace(/program/i, "")] = hl;
                                }
                                else {
                                    obj[segmentIndex.split('_')[0] + convertNumberToString(arr.order) + "&&" +
                                    arr.revision.replace(/program/i, "")] = hl;
                                }
                            }
                            else {
                                obj[segmentIndex.split('_')[0] + convertNumberToString(arr.order) + "&&" + arr.reportName] = hl;
                            }
                        }
                        else {
                            if ($scope.page.match(/^ip/i)) {
                                hl.stage = arr.stage;
                            }
                            obj[segmentIndex.split('_')[0] + convertNumberToString(arr.order) + "&&" + arr.base + " " + arr.program] = hl;
                        }
                    }
                });
            });

            $timeout(function () {
                var data = {};
                data.ptype = $scope.page;
                data.data = obj;
                $http.post("/api/report/headline/" + $scope.page + "/ppt/convert", JSON.stringify(obj))
                    .then(function (ret) {
                        Notification.then({message: 'Report File Generated'});
                        $window.location = "/api/report/headline/" + $scope.page + "/ppt/download";
                    });
            }, 100);

        };

        function formatMilestone(ml) {
            var ele = angular.element("<div>" + ml + "</div>");
            var ps = ele.find("p");
            var ret = [];
            var obj = {};
            angular.forEach(ps, function (hs, idx) {
                var h = angular.element(hs).text();
                var arr = h.split(/\:/);
                obj[arr[0]] = "";
                if (arr.length > 1) {
                    obj[arr[0]] = arr[1].trim();
                }
            });
            ret.push(obj);
            return ret;
        }

        function formatHeadlineIssue(hl) {
            hl = hl.replace(/\<br\>/ig, "\n")//replace break line
                .replace(/\[[0-1]?\d\/[0-3]?\d\/(18|19|20|21)?\d{2}\]/g, "")//replace any date
                .replace(/\&nbsp;/ig, "").replace(/<p>\s*?<\/p>/g, "")//replace empty line
                .replace(/<p>\[\d{2}\/\d{2}\/\d{2}\]<\/p>/ig, "")
                .replace(/<p><\/p>/ig, "").replace(/<pre><\/pre>/ig, "");
            var ret = [];
            if (hl.match(/[0-9a-zA-Z]/)) {
                var hlArr = hl.split(/\<hr\>/);
                var obj = {};
                if (hlArr.length > 0) {
                    angular.forEach(hlArr, function (hs, idx) {
                        if (hs && hs.match(/[0-9a-zA-Z]/)) {
                            var ele = angular.element("<div>" + hs + "</div>");
                            obj = {};
                            if (ele.find("pre").length == 1) {
                                obj['status'] = ele.find("pre")[0].innerText;
                            }
                            else {
                                var ps = ele.find("p");
                                var uls = ele.find("ul");
                                angular.forEach(ps, function (phs, idx) {
                                    var type = angular.element(phs).find("strong");
                                    if (type.length > 0) {
                                        if (angular.element(type[0]).text().match(/issue/i)) {
                                            obj['issue'] = angular.element(phs).text().replace(/issue(\s)*:(\s)*/i, '');
                                        }
                                        else if (angular.element(type[0]).text().match(/status/i)) {
                                            obj['status'] = angular.element(phs).text().replace(/status(\s)*:(\s)*/i, '');
                                        }
                                        else if (angular.element(type[0]).text().match(/next steps/i)) {
                                            obj['next step'] = angular.element(uls).text().replace(/(<([^>]+)>)/ig, '').replace(/\n/g, '');
                                        }
                                        else if (angular.element(type[0]).text().match(/chip affect/i)) {
                                            obj['chip'] = angular.element(uls).text().replace(/(<([^>]+)>)/ig, '').replace(/\n/g, '');
                                        }
                                    }
                                });
                            }

                            if (Object.keys(obj).length === 0) {
                                obj['status'] = hs.replace(/<.*>/g, "");

                            }
                            ret.push(obj);
                        }
                    });
                }
            }
            return ret;
        }
    }
    else if ($scope.type.match(/milestone/i)) {
        if ($scope.page.match(/^(internal|customer|ip)/i)) {
            $http.get("/api/report/milestone/" + $scope.page + "/collect?status=" + $scope.ms.projectStatus)
                .then(function (ret) {
                    if ($scope.page.match(/^internal/i)) {
                        $scope.internalmilestones = ret.data;
                        $scope.generateInternalMilestoneRow(ret.data);
                    }
                    else if ($scope.page.match(/^customer/i)) {
                        $scope.customermilestones = ret.data;
                        $scope.generateCustomerMilestoneRow(ret.data);
                    }
                    else if ($scope.page.match(/^ip/i)) {
                        $scope.ipmilestones = ret.data;
                        $scope.generateIpMilestoneRow(ret.data);
                    }
                });
        }
        else if ($scope.page.match(/^pra/i)) {
            $http.get("/api/report/other/pra/collect")
                .then(function (ret) {
                    $scope.pramilestones = [];
                    $scope.generatePRAMilestoneRow(ret.data);
                });
        }
        else if ($scope.page.match(/^htol/i)) {
            $http.get("/api/report/other/htol/collect")
                .then(function (ret) {
                    $scope.generateHTOLReportRow(ret.data);
                });
        }
        else if ($scope.page.match(/email/i)) {
            $http.get("/api/report/milestone/internal/collect?status=true")
                .then(function (ret) {
                    $scope.internalmilestones = ret.data;
                    $scope.generateInternalMilestoneRow(ret.data);
                });
            $http.get("/api/report/milestone/customer/collect?status=true")
                .then(function (ret) {
                    $scope.customermilestones = ret.data;
                    $scope.generateCustomerMilestoneRow(ret.data);
                });
            $http.get("/api/report/milestone/ip/collect?status=true")
                .then(function (ret) {
                    $scope.ipmilestones = ret.data;
                    $scope.generateIpMilestoneRow(ret.data);
                });
        }

        $scope.refreshEmailMilestoneReport = function () {
            $http.get("/api/report/milestone/internal/collect?reload=1&status=" + $scope.ms.projectStatus)
                .then(function (ret) {
                    $scope.internalmilestones = ret.data;
                    $scope.generateInternalMilestoneRow(ret.data);
                });
            $http.get("/api/report/milestone/customer/collect?reload=1&status=" + $scope.ms.projectStatus)
                .then(function (ret) {
                    $scope.customermilestones = ret.data;
                    $scope.generateCustomerMilestoneRow(ret.data);
                });
            $http.get("/api/report/milestone/ip/collect?reload=1&status=" + $scope.ms.projectStatus)
                .then(function (ret) {
                    $scope.ipmilestones = ret.data;
                    $scope.generateIpMilestoneRow(ret.data);
                });
        };

        $scope.refreshInternalMilestoneReport = function () {
            $http.get("/api/report/milestone/internal/collect?reload=1&status=" + $scope.ms.projectStatus)
                .then(function (ret) {
                    $scope.internalmilestones = ret.data;
                    $scope.generateInternalMilestoneRow(ret.data);
                });
        };
        $scope.refreshIpMilestoneReport = function () {
            $http.get("/api/report/milestone/ip/collect?reload=1&status=" + $scope.ms.projectStatus)
                .then(function (ret) {
                    $scope.ipmilestones = ret.data;
                    $scope.generateIpMilestoneRow(ret.data);
                });
        };
        $scope.refreshCustomerMilestoneReport = function () {
            $http.get("/api/report/milestone/customer/collect?reload=1&status=" + $scope.ms.projectStatus)
                .then(function (ret) {
                    $scope.customermilestones = ret.data;
                    $scope.generateCustomerMilestoneRow(ret.data);
                });
        };

        $scope.refreshPRAMilestoneReport = function () {
            $http.get("/api/report/other/pra/collect?reload=1")
                .then(function (ret) {
                    $scope.pramilestones = [];
                    $scope.generatePRAMilestoneRow(ret.data);
                });
        };
        $scope.refreshHTOLMilestoneReport = function () {
            $http.get("/api/report/other/htol/collect?reload=1")
                .then(function (ret) {
                    $scope.generateHTOLReportRow(ret.data);
                });
        };

        $scope.$watch('ms.viewNote', function (val) {
            if ($scope.page.match(/internal/i)) {
                if (typeof $scope.internalmilestones != 'undefined') {
                    $scope.generateInternalMilestoneRow($scope.internalmilestones);
                }
            }
            else if ($scope.page.match(/customer/i)) {
                if (typeof $scope.customermilestones != 'undefined') {
                    $scope.generateCustomerMilestoneRow($scope.customermilestones);
                }
            }
            else if ($scope.page.match(/^ip/i)) {
                if (typeof $scope.ipmilestones != 'undefined') {
                    $scope.generateIpMilestoneRow($scope.ipmilestones);
                }
            }
            else {
                if (typeof $scope.internalmilestones != 'undefined') {
                    $scope.generateInternalMilestoneRow($scope.internalmilestones);
                }
                if (typeof $scope.customermilestones != 'undefined') {
                    $scope.generateCustomerMilestoneRow($scope.customermilestones);
                }
            }
        });
        $scope.$watch('ms.projectStatus', function (val) {
            if ($scope.page.match(/^(internal|customer|ip)/i)) {
                $http.get("/api/report/milestone/" + $scope.page + "/collect?status=" + val)
                    .then(function (ret) {
                        if ($scope.page.match(/^internal/i)) {
                            $scope.internalmilestones = ret.data;
                            $scope.generateInternalMilestoneRow(ret.data);
                        }
                        else if ($scope.page.match(/^customer/i)) {
                            $scope.customermilestones = ret.data;
                            $scope.generateCustomerMilestoneRow(ret.data);
                        }
                        else if ($scope.page.match(/^ip/i)) {
                            $scope.ipmilestones = ret.data;
                            $scope.generateIpMilestoneRow(ret.data);
                        }
                    });
            }
        });

        $scope.generateInternalMilestoneRow = function (ret) {
            $scope.internalrows = [];
            angular.forEach(ret, function (arr, segment) {
                var row = "<th colspan='13'  style='padding: 5px; text-align:center; vertical-align: middle;background-color:#C0C0C0; font-size:14px; border: 1px solid #ddd;'>" + segment.split(/\_/)[1] + "</th>";
                $scope.internalrows.push(row);
                row = '';
                angular.forEach(arr, function (arr2, program) {
                    if ($scope.ms.viewNote) {
                        row += "<td rowspan='" + parseInt(Object.keys(arr2['revision']).length) * 2 +
                            "' style='padding: 5px; border-collapse:collapse; text-align:center; vertical-align: middle; width: 13%; border: 1px solid #ddd;" + arr2['color'] + "'>" + program;
                    }
                    else {
                        row += "<td rowspan='" + parseInt(Object.keys(arr2['revision']).length) +
                            "' style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 13%; border: 1px solid #ddd;" + arr2['color'] + "'>" + program;
                    }
                    angular.forEach(arr2['revision'], function (arr3, revision) {
                        if ($scope.ms.viewNote) {
                            row += "<td rowspan='2' style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 10%; border: 1px solid #ddd;" + arr3.color + "'>" +
                                "<a href='" + arr3['url'] + "' style='" + arr3.color + "'>" + revision + "</a></td>";
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 10%; border: 1px solid #ddd;" + arr3.color + "'>" +
                                "<a href='" + arr3['url'] + "' style='" + arr3.color + "'>" + revision + "</a></td>";
                        }
                        if (typeof arr3.milestone['CA'] != 'undefined') {
                            row += arr3.milestone['CA'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['PC'] != 'undefined') {
                            row += arr3.milestone['PC'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['RTL_FREEZE'] != 'undefined') {
                            row += arr3.milestone['RTL_FREEZE'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['T/O'] != 'undefined') {
                            row += arr3.milestone['T/O'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['ENG_SAMPLE'] != 'undefined') {
                            row += arr3.milestone['ENG_SAMPLE'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['SDK_BRINGUP'] != 'undefined') {
                            row += arr3.milestone['SDK_BRINGUP'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['DVT_COMPLETE'] != 'undefined') {
                            row += arr3.milestone['DVT_COMPLETE'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['RESPIN_T/O_TARGET'] != 'undefined') {
                            row += arr3.milestone['RESPIN_T/O_TARGET'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['QUAL_COMPLETE'] != 'undefined') {
                            row += arr3.milestone['QUAL_COMPLETE'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['SDK_GA'] != 'undefined') {
                            row += arr3.milestone['SDK_GA'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse;ext-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['PRA'] != 'undefined') {
                            row += arr3.milestone['PRA'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        $scope.internalrows.push(row);
                        row = '';
                        if ($scope.ms.viewNote) {
                            row += arr3['note'];
                            $scope.internalrows.push(row);
                            row = '';
                        }
                    });
                });
            });
        };

        $scope.generateCustomerMilestoneRow = function (ret) {
            $scope.customerrows = [];
            angular.forEach(ret, function (arr, segment) {
                row = '';
                angular.forEach(arr, function (arr2, program) {
                    angular.forEach(arr2['revision'], function (arr3, revision) {
                        var color = arr3.color;
                        if (color.match(/green/i))
                            color = 'black';
                        if ($scope.ms.viewNote) {
                            row += "<td rowspan='2' style='text-align: left; vertical-align: middle; width: 10%; border: 1px solid #ddd;'><span class='tblack'>" + program.split(/\s(.+)?/)[0] + "</span></td>" +
                                "<td rowspan='2' style='text-align: left; vertical-align: middle; width: 10%; border: 1px solid #ddd;'><a href='" + arr3['url'] + "'>" +
                                "<span style='" + color + "'>" + program.split(/\s(.+)?/)[1] + "</span></a></td>";
                        }
                        else {
                            row += "<td  style='text-align: left; vertical-align: middle; width: 10%; border: 1px solid #ddd;'><span class='tblack'>" + program.split(/\s(.+)?/)[0] + "</span></td>" +
                                "<td style='text-align: left; vertical-align: middle; width: 10%; border: 1px solid #ddd;'><a href='" + arr3['url'] + "'>" +
                                "<span style='" + color + "'>" + program.split(/\s(.+)?/)[1] + "</span></a></td>";
                        }
                        if (typeof arr3['switch_chip'] != 'undefined') {
                            row += arr3['switch_chip'];
                        }
                        else {
                            row += "<td style='text-align:left; vertical-align: middle; width: 10%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3['sdk_fcs'] != 'undefined') {
                            row += arr3['sdk_fcs'];
                        }
                        else {
                            row += "<td style='text-align:center; vertical-align: middle; width: 10%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['PROJECT_COMMIT'] != 'undefined') {
                            row += arr3.milestone['PROJECT_COMMIT'];
                        }
                        else {
                            row += "<td style='text-align:center; vertical-align: middle; width: 10%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['PROTO_BRINGUP_COMPLETE'] != 'undefined') {
                            row += arr3.milestone['PROTO_BRINGUP_COMPLETE'];
                        }
                        else {
                            row += "<td style='text-align:center; vertical-align: middle; width: 10%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['SQA_ENTRY_/_UNIT_TEST_COMPLETE'] != 'undefined') {
                            row += arr3.milestone['SQA_ENTRY_/_UNIT_TEST_COMPLETE'];
                        }
                        else {
                            row += "<td style='text-align:center; vertical-align: middle; width: 10%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['SQA_COMPLETE'] != 'undefined') {
                            row += arr3.milestone['SQA_COMPLETE'];
                        }
                        else {
                            row += "<td style='text-align:center; vertical-align: middle; width: 10%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['PILOT'] != 'undefined') {
                            row += arr3.milestone['PILOT'];
                        }
                        else {
                            row += "<td style='text-align:center; vertical-align: middle; width: 10%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['FCS'] != 'undefined') {
                            row += arr3.milestone['FCS'];
                        }
                        else {
                            row += "<td style='text-align:center; vertical-align: middle; width: 10%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        $scope.customerrows.push(row);
                        row = '';

                        if ($scope.ms.viewNote) {
                            row += arr3['note'];
                            $scope.customerrows.push(row);
                            row = '';
                        }
                    });
                });
            });
        };

        $scope.generateIpMilestoneRow = function (ret) {
            $scope.iprows = [];
            angular.forEach(ret, function (arr, segment) {
                var row = '';
                angular.forEach(arr, function (arr2, program) {
                    if ($scope.ms.viewNote) {
                        row += "<td rowspan='" + parseInt(Object.keys(arr2['revision']).length) * 2 +
                            "' style='padding: 5px; border-collapse:collapse; text-align:center; vertical-align: middle; width: 13%; border: 1px solid #ddd;" + arr2['color'] + "'>" + program;
                    }
                    else {
                        row += "<td rowspan='" + parseInt(Object.keys(arr2['revision']).length) +
                            "' style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 13%; border: 1px solid #ddd;" + arr2['color'] + "'>" + program;
                    }
                    angular.forEach(arr2['revision'], function (arr3, revision) {
                        if ($scope.ms.viewNote) {
                            row += "<td rowspan='2' style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 10%; border: 1px solid #ddd;" + arr3.color + "'>" +
                                "<a href='" + arr3['url'] + "' style='" + arr3.color + "'>" + revision + "</a></td>";
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 10%; border: 1px solid #ddd;" + arr3.color + "'>" +
                                "<a href='" + arr3['url'] + "' style='" + arr3.color + "'>" + revision + "</a></td>";
                        }
                        if (typeof arr3.milestone['PRLM_RTL/VG'] != 'undefined') {
                            row += arr3.milestone['PRLM_RTL/VG'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['PRLM_LEF'] != 'undefined') {
                            row += arr3.milestone['PRLM_LEF'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['PRLM_LIB'] != 'undefined') {
                            row += arr3.milestone['PRLM_LIB'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['INT_VGS'] != 'undefined') {
                            row += arr3.milestone['INT_VGS'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['INT_LEF'] != 'undefined') {
                            row += arr3.milestone['INT_LEF'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['INT_LIB'] != 'undefined') {
                            row += arr3.milestone['INT_LIB'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['FINAL_VGS'] != 'undefined') {
                            row += arr3.milestone['FINAL_VGS'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['FINAL_LIB'] != 'undefined') {
                            row += arr3.milestone['FINAL_LIB'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        if (typeof arr3.milestone['FINAL_GDS'] != 'undefined') {
                            row += arr3.milestone['FINAL_GDS'];
                        }
                        else {
                            row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; width: 7%; border: 1px solid #ddd;'>&nbsp;</td>";
                        }
                        $scope.iprows.push(row);
                        row = '';
                        if ($scope.ms.viewNote) {
                            row += arr3['note'];
                            $scope.iprows.push(row);
                            row = '';
                        }
                    });
                });
            });
        };

        $scope.generatePRAMilestoneRow = function (ret) {
            $scope.pramilestones = [];
            angular.forEach(ret, function (arr, programName) {
                var obj = {};
                var nameArr = programName.split('&&');
                obj.chip = nameArr[0];
                obj.revision = nameArr[1];
                obj['to'] = '';
                obj['to month'] = '';
                obj['to quarter'] = '';
                obj['to year'] = '';
                obj['pra'] = '';
                obj['pra month'] = '';
                obj['pra quarter'] = '';
                obj['pra year'] = '';
                obj['process node'] = "";
                if ('process' in arr) {
                    obj['process node'] = arr['process'];
                }
                if ('milestone' in arr) {
                    var milestone = arr['milestone'];
                    if ('T/O' in milestone) {
                        obj['to'] = milestone['T/O'];
                    }
                    if ('T/O Month' in milestone) {
                        obj['to month'] = milestone['T/O Month'];
                    }
                    if ('T/O Year' in milestone) {
                        obj['to year'] = milestone['T/O Year'];
                    }
                    if ('T/O Quarter' in milestone) {
                        obj['to quarter'] = milestone['T/O Quarter'];
                    }
                    if ('PRA' in milestone) {
                        obj['pra'] = milestone['PRA'];
                    }
                    if ('PRA Month' in milestone) {
                        obj['pra month'] = milestone['PRA Month'];
                    }
                    if ('PRA Year' in milestone) {
                        obj['pra year'] = milestone['PRA Year'];
                    }
                    if ('PRA Quarter' in milestone) {
                        obj['pra quarter'] = milestone['PRA Quarter'];
                    }
                    if ('PRA Target' in milestone) {
                        obj['pra target'] = milestone['PRA Target'];
                    }
                }
                $scope.pramilestones.push(obj);
            });
            $scope.praGridOptions.data = $scope.pramilestones;
        };
        $scope.generateHTOLReportRow = function (ret) {
            $scope.htolrows = [];
            angular.forEach(ret, function (arr, segment) {
                var row = "<th colspan='10'  style='padding: 5px; text-align:center; vertical-align: middle;background-color:#C0C0C0; font-size:14px; border: 1px solid #ddd;'>" + segment.split(/\_/)[1] + "</th>";
                $scope.htolrows.push(row);
                row = '';
                angular.forEach(arr, function (arr2, program) {
                    angular.forEach(arr2['revision'], function (arr3, revision) {
                        row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; " +
                            "width: 13%; border: 1px solid #ddd;'>" + arr3['base_num'] + "</td>";
                        row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; " +
                            "width: 13%; border: 1px solid #ddd;'>" + program + "</td>";
                        row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; " +
                            "width: 7%; border: 1px solid #ddd;'>" + arr3['reduced_pm'] + "</td>";
                        row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; " +
                            "width: 7%; border: 1px solid #ddd;'>" + arr3['processnode'] + "</td>";
                        row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; " +
                            "width: 7%; border: 1px solid #ddd;'>" + revision.toUpperCase() + "</td>";

                        row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; " +
                            "width: 13%; border: 1px solid #ddd;'>" + arr3['t/o'] + "</td>";
                        row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; " +
                            "width: 10%; border: 1px solid #ddd;'>" + arr3['package'] + "</td>";
                        row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; " +
                            "width: 10%; border: 1px solid #ddd;'>" + arr3['maxpower'] + "</td>";
                        row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; " +
                            "width: 10%; border: 1px solid #ddd;'>" + arr3['htolpower'] + "</td>";
                        row += "<td style='padding: 5px; border-collapse: collapse; text-align:center; vertical-align: middle; " +
                            "width: 10%; border: 1px solid #ddd;'>" + arr3['respin_t/o_target'] + "</td>";

                        $scope.htolrows.push(row);
                        row = '';
                    });
                });
            });
        };
    }
    else if ($scope.type.match(/outlook/i)) {
        $http.get("/api/report/" + $scope.type + "/" + $scope.page + "/collect")
            .then(function (ret) {
                $scope.outlooks = ret.data;
            });

        $scope.refreshOutlookReport = function () {
            Notification.info({
                message: 'Please Wait!! Loading Latest outlook Data....',
                delay: 5000,
                replaceMessage: true,
                positionY: 'top'
            });
            $http.get("/api/report/" + $scope.type + "/" + $scope.page + "/collect?reload=1")
                .then(function (ret) {
                    $scope.outlooks = ret.data;
                });
        };
    }
    else if ($scope.type.match(/information/i)) {
        $scope.infoExport = {};
        $http.get("/api/report/" + $scope.type + "/" + $scope.page + "/collect")
            .then(function (ret) {
                $scope.infos = ret.data;
                $scope.infoGridOptions.minRowsToShow = 23;
                $scope.infoGridOptions.data = generateInformationReportRow(ret.data.data);
                $scope.infoGridOptions.columnDefs = ret.data.title;
            });

        $scope.refreshInfoReport = function () {
            $http.get("/api/report/" + $scope.type + "/" + $scope.page + "/collect?reload=1")
                .then(function (ret) {
                    $scope.infos = ret.data;
                    $scope.infoGridOptions.minRowsToShow = 23;
                    $scope.infoGridOptions.data = generateInformationReportRow(ret.data.data);
                    $scope.infoGridOptions.columnDefs = ret.data.title;
                });
        };

        $scope.exportInfoReport = function (title, fname) {
            $scope.infoExport = generateInformationFileData($scope.infos);
            var html = "<table class='table table-bordered table-condensed table-header-bg' border='1' cellpadding='0' cellspacing='0'>";
            html += "<thead>" +
                "<tr>" + $scope.infoExport.header.first + "</tr>" +
                "<tr>" + $scope.infoExport.header.second + "</tr>" +
                "</thead>" +
                "<tbody>";
            angular.forEach($scope.infoExport.data, function (row, index) {
                html += "<tr>" + row + "</tr>";
            });
            html += "</tbody></table>";
            $scope.exportHtmlDataToExcel(html, title, fname);

        };

        function generateInformationReportRow(input) {
            var ret = [];
            angular.forEach(input, function (revdata, index) {
                var obj = {};
                angular.forEach(revdata, function (value, key) {
                    if ('latest' in value) {
                        obj[key] = value.latest;
                    }
                });
                ret.push(obj);
            });
            return ret;
        }

        function generateInformationFileData(input) {
            var ret = {};
            ret.header = {};
            ret.data = {};
            var data = [];
            if ('keys' in input || 'data' in input) {
                var keys = input.keys;
                var header1 = "";
                var header2 = "";
                angular.forEach(keys, function (key, index) {
                    if (key.match(/^(program|revision|base die|status|ca|pc)/i)) {
                        header1 += "<th>" + key.toUpperCase() + "</th>";
                        header2 += "<th></th>";
                    }
                    else {
                        header1 += "<th colspan='5'>" + key.toUpperCase() + "</th>";
                        header2 += "<th>CA</th><th>PC</th><th>ECR</th><th>Current</th><th>TO/Final</th>";
                    }
                });

                var dataarr = input.data;
                var row = "";
                angular.forEach(dataarr, function (rev, index) {
                    row = "";
                    angular.forEach(keys, function (key, i) {
                        var k = key.replace(/\s+/ig, '').toLowerCase();
                        if (k in rev) {
                            var obj = rev[k];
                            if (key.match(/^(program|revision|base die|status|ca|pc)/i)) {
                                if ('latest' in obj)
                                    row += "<td>" + obj.latest + "</td>";
                                else
                                    row += "<td></td>";
                            }
                            else {
                                if ('ca' in obj || 'pc' in obj || 'ecr' in obj || 'to/final' in obj || 'current' in obj) {
                                    if ('ca' in obj)
                                        row += "<td>" + obj.ca + "</td>";
                                    else
                                        row += "<td></td>";
                                    if ('pc' in obj)
                                        row += "<td>" + obj.pc + "</td>";
                                    else
                                        row += "<td></td>";
                                    if ('ecr' in obj)
                                        row += "<td>" + obj.ecr + "</td>";
                                    else
                                        row += "<td></td>";
                                    if ('current' in obj)
                                        row += "<td>" + obj.current + "</td>";
                                    else
                                        row += "<td></td>";
                                    if ('to/final' in obj)
                                        row += "<td>" + obj['to/final'] + "</td>";
                                    else
                                        row += "<td></td>";
                                }
                                else {
                                    if ('latest' in obj)
                                        row += "<td>" + obj.latest + "</td>";
                                    else
                                        row += "<td></td>";
                                }

                            }
                        }
                        else {
                            row += "<td></td>";
                        }

                    });
                    data.push(row);
                });
            }
            ret.header.first = header1;
            ret.header.second = header2;
            ret.data = data;
            return ret;
        }
    }


});