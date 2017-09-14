Array.prototype.pushArray = function (arr) {
    this.push.apply(this, arr);
};
App.controller('ReportCtrl', function ($scope, $rootScope, $filter, $http, $sce, $timeout, $stateParams, $location, $window, $interval, uiGridConstants,
                                       $localStorage, uiGridGroupingConstants, Notification, authenticationFactory) {
    $scope.permission = {};
    $scope.$watch('$root.wbi', function () {
        $localStorage.wbi = $rootScope.wbi;
        $scope.permission = $rootScope.wbi.permission;
    }, true);

    $scope.type = $stateParams.type;
    $scope.rtype = $stateParams.rtype;
    $scope.page = $stateParams.page;
    if (!navigator.appVersion.match(/chrome/i)) {
        alert("WBI only supports Chrome. Please use it when accessing WBI");
    }
    $scope.headlineReportLoaded = false;
    $scope.internalMilestoneReportLoaded = false;
    $scope.ipMilestoneReportLoaded = false;
    $scope.praMilestoneReportLoaded = false;
    $scope.customerMilestoneReportLoaded = false;
    $scope.htolReportLoaded = false;

    $scope.infos = {};
    $scope.infos.data = [];
    $scope.infos.column = [];

    $scope.pramilestones = [];

    $scope.go = function (url) {
        $location.path(url);
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
            $http.post("/report/sendEmailReport", {data: content})
                .success(function (ret) {
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
                        Notification.success({
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
    if ($scope.rtype.match(/indicator/i)) {
        if ($scope.type.match(/milestone/i)) {
            if ($scope.page.match(/^(internal|customer|ip)/i)) {
                $http.get("/report/milestone/" + $scope.page + "/collect/report?status=" + $scope.ms.projectStatus)
                    .success(function (ret) {
                        if ($scope.page.match(/^internal/i)) {
                            $scope.internalmilestones = ret;
                            $scope.generateInternalMilestoneRow(ret);
                            $scope.internalMilestoneReportLoaded = true;
                        }
                        else if ($scope.page.match(/^customer/i)) {
                            $scope.customermilestones = ret;
                            $scope.generateCustomerMilestoneRow(ret);
                            $scope.customerMilestoneReportLoaded = true;
                        }
                        else if ($scope.page.match(/^ip/i)) {
                            $scope.ipmilestones = ret;
                            $scope.generateIpMilestoneRow(ret);
                            $scope.ipMilestoneReportLoaded = true;
                        }
                    });
            }
            else if ($scope.page.match(/^pra/i)) {
                $http.get("/report/other/pra/collect/report")
                    .success(function (ret) {
                        $scope.pramilestones = [];
                        $scope.generatePRAMilestoneRow(ret);
                        $scope.praMilestoneReportLoaded = true;
                    });
            }
            else if ($scope.page.match(/^htol/i)) {
                $http.get("/report/other/htol/collect/report")
                    .success(function (ret) {
                        $scope.generateHTOLReportRow(ret);
                        $scope.htolReportLoaded = true;
                    });
            }
            else if ($scope.page.match(/email/i)) {
                $http.get("/report/milestone/internal/collect/report?status=true")
                    .success(function (ret) {
                        $scope.internalmilestones = ret;
                        $scope.generateInternalMilestoneRow(ret);
                        $scope.internalMilestoneReportLoaded = true;
                    });
                $http.get("/report/milestone/customer/collect/report?status=true")
                    .success(function (ret) {
                        $scope.customermilestones = ret;
                        $scope.generateCustomerMilestoneRow(ret);
                        $scope.customerMilestoneReportLoaded = true;
                    });
                $http.get("/report/milestone/ip/collect/report?status=true")
                    .success(function (ret) {
                        $scope.ipmilestones = ret;
                        $scope.generateIpMilestoneRow(ret);
                        $scope.ipMilestoneReportLoaded = true;
                    });
            }

            $scope.refreshEmailMilestoneReport = function () {
                $scope.internalMilestoneReportLoaded = false;
                $scope.customerMilestoneReportLoaded = false;
                $scope.ipMilestoneReportLoaded = false;
                Notification.info({
                    message: 'Please Wait!! Loading Latest Report Data....',
                    delay: 5000,
                    replaceMessage: true,
                    positionY: 'top'
                });
                $http.get("/report/milestone/internal/collect/report?reload=1&status=" + $scope.ms.projectStatus)
                    .success(function (ret) {
                        $scope.internalmilestones = ret;
                        $scope.generateInternalMilestoneRow(ret);
                        $scope.internalMilestoneReportLoaded = true;
                    });
                $http.get("/report/milestone/customer/collect/report?reload=1&status=" + $scope.ms.projectStatus)
                    .success(function (ret) {
                        $scope.customermilestones = ret;
                        $scope.generateCustomerMilestoneRow(ret);
                        $scope.customerMilestoneReportLoaded = true;
                    });
                $http.get("/report/milestone/ip/collect/report?reload=1&status=" + $scope.ms.projectStatus)
                    .success(function (ret) {
                        $scope.ipmilestones = ret;
                        $scope.generateIpMilestoneRow(ret);
                        $scope.ipMilestoneReportLoaded = true;
                    });
            };

            $scope.refreshInternalMilestoneReport = function () {
                $scope.internalMilestoneReportLoaded = false;
                Notification.info({
                    message: 'Please Wait!! Loading Latest Report Data....',
                    delay: 5000,
                    replaceMessage: true,
                    positionY: 'top'
                });
                $http.get("/report/milestone/internal/collect/report?reload=1&status=" + $scope.ms.projectStatus)
                    .success(function (ret) {
                        $scope.internalmilestones = ret;
                        $scope.generateInternalMilestoneRow(ret);
                        $scope.internalMilestoneReportLoaded = true;
                    });
            };
            $scope.refreshIpMilestoneReport = function () {
                $scope.ipMilestoneReportLoaded = false;
                Notification.info({
                    message: 'Please Wait!! Loading Latest Report Data....',
                    delay: 5000,
                    replaceMessage: true,
                    positionY: 'top'
                });
                $http.get("/report/milestone/ip/collect/report?reload=1&status=" + $scope.ms.projectStatus)
                    .success(function (ret) {
                        $scope.ipmilestones = ret;
                        $scope.generateIpMilestoneRow(ret);
                        $scope.ipMilestoneReportLoaded = true;
                    });
            };
            $scope.refreshCustomerMilestoneReport = function () {
                $scope.customerMilestoneReportLoaded = false;
                Notification.info({
                    message: 'Please Wait!! Loading Latest Report Data....',
                    delay: 5000,
                    replaceMessage: true,
                    positionY: 'top'
                });
                $http.get("/report/milestone/customer/collect/report?reload=1&status=" + $scope.ms.projectStatus)
                    .success(function (ret) {
                        $scope.customermilestones = ret;
                        $scope.generateCustomerMilestoneRow(ret);
                        $scope.customerMilestoneReportLoaded = true;
                    });
            };

            $scope.refreshPRAMilestoneReport = function () {
                $scope.praMilestoneReportLoaded = false;
                Notification.info({
                    message: 'Please Wait!! Loading Latest Report Data....',
                    delay: 5000,
                    replaceMessage: true,
                    positionY: 'top'
                });
                $http.get("/report/other/pra/collect/report?reload=1")
                    .success(function (ret) {
                        $scope.pramilestones = [];
                        $scope.generatePRAMilestoneRow(ret);
                        $scope.praMilestoneReportLoaded = true;
                    });
            };
            $scope.refreshHTOLReport = function () {
                $scope.htolReportLoaded = false;
                Notification.info({
                    message: 'Please Wait!! Loading Latest Report Data....',
                    delay: 5000,
                    replaceMessage: true,
                    positionY: 'top'
                });
                $http.get("/report/other/htol/collect/report?reload=1")
                    .success(function (ret) {
                        $scope.generateHTOLReportRow(ret);
                        $scope.htolReportLoaded = true;
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
                    $http.get("/report/milestone/" + $scope.page + "/collect/report?status=" + val)
                        .success(function (ret) {
                            if ($scope.page.match(/^internal/i)) {
                                $scope.internalmilestones = ret;
                                $scope.generateInternalMilestoneRow(ret);
                                $scope.internalMilestoneReportLoaded = true;
                            }
                            else if ($scope.page.match(/^customer/i)) {
                                $scope.customermilestones = ret;
                                $scope.generateCustomerMilestoneRow(ret);
                                $scope.customerMilestoneReportLoaded = true;
                            }
                            else if ($scope.page.match(/^ip/i)) {
                                $scope.ipmilestones = ret;
                                $scope.generateIpMilestoneRow(ret);
                                $scope.ipMilestoneReportLoaded = true;
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
        else if ($scope.type.match(/headline/i)) {
            $http.get("/report/" + $scope.type + "/" + $scope.page + "/collect/report")
                .success(function (ret) {
                    $scope.headlines = ret;
                    if ($scope.page.match(/internal/i))
                        $scope.generateInternalHeadlineRow(ret);
                    else if ($scope.page.match(/customer/i))
                        $scope.generateCustomerHeadlineRow(ret);
                    else if ($scope.page.match(/ip/i))
                        $scope.generateIPHeadlineRow(ret);
                    $scope.headlineReportLoaded = true;
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
                $scope.headlineReportLoaded = false;
                Notification.info({
                    message: 'Please Wait!! Loading Latest Headline Data....',
                    delay: 5000,
                    replaceMessage: true,
                    positionY: 'top'
                });
                $http.get("/report/" + $scope.type + "/" + $scope.page + "/collect/report?reload=1")
                    .success(function (ret) {
                        $scope.headlines = ret;
                        $scope.headlineReportLoaded = true;
                    });
            };

            $scope.saveHeadlineReportTable = function () {
                var completeRequest = 0;
                $scope.headlineReportLoaded = false;
                $http.post('/revision/saveOrder', {'data': $scope.customerheadlinerows})
                    .success(function (result) {
                        Notification.success({message: result.data, delay: 1000});
                        $scope.headlineReportLoaded = true;
                    }).error(function (ret) {
                    Notification.error({message: ret.data, delay: 5000, title: ret.code});
                    $scope.headlineReportLoaded = true;
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
                    $http.post("/report/headline/" + $scope.page + "/ppt/convert", JSON.stringify(obj))
                        .success(function (ret) {
                            Notification.success({message: 'Report File Generated'});
                            $window.location = "/report/headline/" + $scope.page + "/ppt/download";
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
        else if ($scope.type.match(/outlook/i)) {
            $http.get("/report/" + $scope.type + "/" + $scope.page + "/collect/report")
                .success(function (ret) {
                    $scope.outlooks = ret;
                    $scope.outlookReportLoaded = true;
                });

            $scope.refreshOutlookReport = function () {
                Notification.info({
                    message: 'Please Wait!! Loading Latest outlook Data....',
                    delay: 5000,
                    replaceMessage: true,
                    positionY: 'top'
                });
                $scope.outlookReportLoaded = false;
                $http.get("/report/" + $scope.type + "/" + $scope.page + "/collect/report?reload=1")
                    .success(function (ret) {
                        $scope.outlooks = ret;
                        $scope.outlookReportLoaded = true;
                    });
            };
        }
        else if ($scope.type.match(/information/i)) {
            $scope.infoReportLoaded = false;
            $scope.infoExport = {};
            $http.get("/report/" + $scope.type + "/" + $scope.page + "/collect/report")
                .success(function (ret) {
                    $scope.infos = ret;
                    $scope.infoReportLoaded = true;
                    $scope.infoGridOptions.minRowsToShow = 23;
                    $scope.infoGridOptions.data = generateInformationReportRow(ret.data);
                    $scope.infoGridOptions.columnDefs = ret.title;
                });

            $scope.refreshInfoReport = function () {
                Notification.info({
                    message: 'Please Wait!! Loading Latest outlook Data....',
                    delay: 5000,
                    replaceMessage: true,
                    positionY: 'top'
                });
                $scope.infoReportLoaded = false;
                $http.get("/report/" + $scope.type + "/" + $scope.page + "/collect/report?reload=1")
                    .success(function (ret) {
                        $scope.infos = ret;
                        $scope.infoReportLoaded = true;
                        $scope.infoGridOptions.minRowsToShow = 23;
                        $scope.infoGridOptions.data = generateInformationReportRow(ret.data);
                        $scope.infoGridOptions.columnDefs = ret.title;
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
    }
    else if ($scope.rtype.match(/resource/i)) {
        $scope.resourceDate = {};
        $scope.resourceDate.fromDate = '';
        $scope.resourceDate.toDate = '';
        $scope.resourceDate.intervalGroup = 'month';
        $scope.resourceDatePickerOptions = {
            startingDay: 1,
            datepickerMode: 'month',
            minMode: 'month',
            'show-weeks': false
        };
        $scope.clearResourceCache = function () {
            $http.post('/resource/report/clearCache').success(function (ret) {
                Notification.success({
                    message: 'Resource Cache cleared',
                    delay: 5000,
                    replaceMessage: true,
                    positionY: 'top',
                    positionX: 'center'
                });
            });
        };
        $scope.startDateOpenStatus = false;
        $scope.openStartDatePicker = function ($event) {
            $scope.startDateOpenStatus = true;
        };
        $scope.toDateOpenStatus = false;
        $scope.openEndDatePicker = function ($event) {
            $scope.toDateOpenStatus = true;
        };
        if ($scope.page.match(/dashboard/i)) {
            $scope.resourceReportLoaded = false;
            $http.get('/resource/report/generateCurrentMonthStatus?groupBy=design_center')
                .success(function (result) {
                    $scope.locPieChartConfig.series[0].data = result;
                    $scope.resourceReportLoaded = true;
                }).error(function (data, code) {
                console.log(data);
                $scope.resourceReportLoaded = true;
            });
            $http.get('/resource/report/generateCurrentMonthStatus?groupBy=skill_name')
                .success(function (result) {
                    $scope.skillPieChartConfig.series[0].data = result;
                    $scope.resourceReportLoaded = true;
                }).error(function (data, code) {
                console.log(data);
                $scope.resourceReportLoaded = true;
            });
            $http.get('/resource/report/generateCurrentMonthStatus?groupBy=project_name')
                .success(function (result) {
                    $scope.projectPieChartConfig.series[0].data = result;
                    $scope.resourceReportLoaded = true;
                }).error(function (data, code) {
                console.log(data);
                $scope.resourceReportLoaded = true;
            });

            $http.get('/resource/report/generateTrendStatus?groupBy=design_center&returnType=chart')
                .success(function (result) {
                    $scope.locTrendColumnChartConfig.xAxis.categories = result.category;
                    $scope.locTrendColumnChartConfig.series = result.series;
                    $scope.resourceReportLoaded = true;
                }).error(function (data, code) {
                console.log(data);
                $scope.resourceReportLoaded = true;
            });
            $http.get('/resource/report/generateTrendStatus?groupBy=skill_name&returnType=chart')
                .success(function (result) {
                    $scope.skillTrendColumnChartConfig.xAxis.categories = result.category;
                    $scope.skillTrendColumnChartConfig.series = result.series;
                    $scope.resourceReportLoaded = true;
                }).error(function (data, code) {
                console.log(data);
                $scope.resourceReportLoaded = true;
            });
            $http.get('/resource/report/generateTrendStatus?groupBy=project_name&returnType=chart')
                .success(function (result) {
                    $scope.projectTrendColumnChartConfig.xAxis.categories = result.category;
                    $scope.projectTrendColumnChartConfig.series = result.series;
                    $scope.resourceReportLoaded = true;
                }).error(function (data, code) {
                console.log(data);
                $scope.resourceReportLoaded = true;
            });
        }
        else if ($scope.page.match(/settings/i)) {
            $scope.rs = {};
            $scope.rs.type = '';
            $scope.rs.newgroup = {};
            $scope.rs.exist = {};
            $scope.rs.status = true;
            $scope.rs.newtypeselected = '';
            $scope.rs.remove_project_list = "";
            $scope.rs.new_project_list = "";
            $scope.rs.type_list = [];
            $scope.rs.projectselected = [];

            $http.get('/resource/report/getAllProjectGroup')
                .success(function (result) {
                    $scope.rs.type_list = result;
                });

            $http.get('/resource/report/getAllDistinctProject')
                .success(function (result) {
                    $scope.rs.all_project_list = result;
                }).error(function (data, code) {
                console.log(data);
            });

            Array.prototype.diff = function (a) {
                return this.filter(function (i) {
                    return a.indexOf(i) < 0;
                });
            };
            $scope.$watch('rs.exist.object', function (val) {
                if (typeof val === "object") {
                    if (val.name.match(/[0-9a-zA-Z]/)) {
                        $scope.rs.exist.project_list = val.projects;
                        $scope.rs.exist.includeInReport = val.inReport;
                        $scope.rs.exist.type = val.type;
                        $scope.rs.exist.id = val.id;
                        $scope.rs.exist.name = val.name;
                    }
                }
            });
            $scope.saveResourceProjectSettings = function () {
                $http.post("/resource/report/saveProjectClassificationList", {
                    "id": $scope.rs.exist.id,
                    "name": $scope.rs.exist.name,
                    "list": $scope.rs.exist.project_list,
                    "status": $scope.rs.exist.includeInReport
                })
                    .success(function (ret) {
                        $http.get('/resource/report/getDistinctProjectByType?id=' + $scope.rs.exist.id)
                            .success(function (result) {
                                $scope.rs.exist.project_list = result.projects;
                                $scope.rs.exist.includeInReport = result.inReport;
                                $scope.rs.exist.type = result.type;
                            }).error(function (data, code) {
                            console.log(data);
                        });
                    });
            };
            $scope.deleteResourceProjectSettings = function () {
                $http.post("/resource/report/deleteProjectClassificationList", {"id": $scope.rs.exist.id})
                    .success(function (ret) {
                        $http.get('/resource/report/getAllProjectGroup')
                            .success(function (result) {
                                $scope.rs.type_list = result;
                            });
                    });
            };
            $scope.newResourceProjectSettings = function () {
                $http.post("/resource/report/saveProjectClassificationList", {
                    "id": 0,
                    "name": $scope.rs.newgroup.name,
                    "type": $scope.rs.newgroup.type,
                    "status": $scope.rs.newgroup.includeInReport
                })
                    .success(function (ret) {
                        $http.get('/resource/report/getAllProjectGroup')
                            .success(function (result) {
                                $scope.rs.type_list = result;
                                $scope.rs.newgroup = {};
                                $scope.rs.exist = {};
                            });
                    });
            };
        }
        else if ($scope.page.match(/grouping/i)) {
            var groupType = "project";
            if ($scope.page.match(/report/i))
                groupType = "report";
            $scope.monthlyGridOptions = {
                data: [],
                enableColumnMenus: false,
                enableGridMenu: true,
                exporterMenuPdf: true,
                exporterMenuCsv: true,
                treeRowHeaderAlwaysVisible: false,
                enableColumnResizing: true,
                enableFiltering: true,
                onRegisterApi: function (gridApi) {
                    $scope.gridApi = gridApi;
                },
                minRowsToShow: 20
            };
            $http.get('/resource/report/generateProjectTrendStatus?reload=1' + "&intervalGroup=" + $scope.resourceDate.intervalGroup + "&type=" + groupType.toLowerCase())
                .success(function (result) {
                    var cols = [];
                    angular.forEach(result.col, function (arr) {
                        if (arr.name.match(/[0-9]|avg|total/i)) {
                            arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                            arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                aggregation.rendered = aggregation.value.toFixed(2);
                            };
                        }
                        cols.push(arr);
                    });
                    $scope.monthlyGridOptions.columnDefs = cols;
                    $scope.monthlyGridOptions.data = result['data'];
                });

            $scope.generateMonthlyProjectReport = function () {
                var from = '';
                var to = '';
                if (String($scope.resourceDate.fromDate).match(/[0-9]/)) {
                    from = new Date(Date.parse($scope.resourceDate.fromDate)).toLocaleDateString();
                }
                if (String($scope.resourceDate.toDate).match(/[0-9]/)) {
                    to = new Date(Date.parse($scope.resourceDate.toDate)).toLocaleDateString();
                }
                $http.get('/resource/report/generateProjectTrendStatus?reload=1' + "&from=" + from + "&to=" + to + "&intervalGroup=" + $scope.resourceDate.intervalGroup + "&type=" + groupType.toLowerCase())
                    .success(function (result) {
                        var cols = [];
                        angular.forEach(result.col, function (arr) {
                            if (arr.name.match(/[0-9]|avg|total/i)) {
                                arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                                arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                    aggregation.rendered = aggregation.value.toFixed(2);
                                };
                            }
                            cols.push(arr);
                        });
                        $scope.monthlyGridOptions.columnDefs = cols;
                        $scope.monthlyGridOptions.data = result['data'];
                    });
            };
        }
        else if ($scope.page.match(/skill/i)) {
            $scope.skillGridOptions = {
                data: [],
                enableColumnMenus: false,
                enableGridMenu: true,
                exporterMenuPdf: false,
                enableColumnResizing: true,
                exporterCsvFilename: 'skill_resource.csv',
                enableFiltering: true,
                onRegisterApi: function (gridApi) {
                    $scope.gridApi = gridApi;
                },
                minRowsToShow: 20
            };
            $http.get('/resource/report/generateTrendStatus?groupBy=skill_name&returnType=chart')
                .success(function (result) {
                    $scope.skillTrendColumnChartConfig.xAxis.categories = result.category;
                    $scope.skillTrendColumnChartConfig.series = result.series;
                }).error(function (data, code) {
                console.log(data);
            });
            $http.get('/resource/report/generateTrendStatus?groupBy=skill_name&returnType=table')
                .success(function (result) {
                    $scope.skillGridOptions.data = result['data'];
                    $scope.skillGridOptions.columnDefs = result['col'];
                }).error(function (data, code) {
                console.log(data);
            });
            $scope.generateSkillReport = function () {
                var from = '';
                var to = '';
                if (String($scope.resourceDate.fromDate).match(/[0-9]/)) {
                    from = new Date(Date.parse($scope.resourceDate.fromDate)).toLocaleDateString();
                }
                if (String($scope.resourceDate.toDate).match(/[0-9]/)) {
                    to = new Date(Date.parse($scope.resourceDate.toDate)).toLocaleDateString();
                }
                $http.get('/resource/report/generateTrendStatus?groupBy=skill_name&returnType=chart&from=' + from + "&to=" + to + "&intervalGroup=" + $scope.resourceDate.intervalGroup)
                    .success(function (result) {
                        $scope.skillTrendColumnChartConfig.xAxis.categories = result.category;
                        $scope.skillTrendColumnChartConfig.series = result.series;
                    }).error(function (data, code) {
                    console.log(data);
                });
                $http.get('/resource/report/generateTrendStatus?groupBy=skill_name&returnType=table&from=' + from + "&to=" + to)
                    .success(function (result) {
                        $scope.skillGridOptions.data = result['data'];
                        $scope.skillGridOptions.columnDefs = result['col'];
                    }).error(function (data, code) {
                    console.log(data);
                });
            };
        }
        else if ($scope.page.match(/project$/i)) {
            $scope.projectSkillGridOptions = {
                data: [],
                enableColumnMenus: false,
                enableGridMenu: true,
                exporterMenuPdf: false,
                exporterMenuCsv: false,
                treeRowHeaderAlwaysVisible: false,
                enableColumnResizing: true,
                enableFiltering: true,
                onRegisterApi: function (gridApi) {
                    $scope.gridApi = gridApi;
                },
                minRowsToShow: 20
            };
            $scope.projectGridOptions = {
                data: [],
                enableColumnMenus: false,
                enableGridMenu: true,
                exporterMenuPdf: false,
                exporterMenuCsv: false,
                treeRowHeaderAlwaysVisible: false,
                enableColumnResizing: true,
                enableFiltering: true,
                onRegisterApi: function (gridApi) {
                    $scope.gridApi = gridApi;
                },
                minRowsToShow: 20
            };
            $http.get('/resource/report/generateTrendStatus?groupBy=project_name&returnType=chart')
                .success(function (result) {
                    $scope.projectTrendColumnChartConfig.xAxis.categories = result.category;
                    $scope.projectTrendColumnChartConfig.series = result.series;
                }).error(function (data, code) {
                console.log(data);
            });
            $http.get('/resource/report/generateTrendStatus?groupBy=project_name,skill_name')
                .success(function (result) {
                    var cols = [];
                    angular.forEach(result.col, function (arr) {
                        if (arr.name.match(/([0-9]|total)/i)) {
                            arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                            arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                aggregation.rendered = aggregation.value.toFixed(2);
                            };
                        }
                        cols.push(arr);
                    });
                    $scope.projectSkillGridOptions.data = result['data'];
                    $scope.projectSkillGridOptions.columnDefs = cols;
                });
            $http.get('/resource/report/generateTrendStatus?groupBy=project_name')
                .success(function (result) {
                    var cols = [];
                    angular.forEach(result.col, function (arr) {
                        if (arr.name.match(/([0-9]|total)/i)) {
                            arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                            arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                aggregation.rendered = aggregation.value.toFixed(2);
                            };
                        }
                        cols.push(arr);
                    });
                    $scope.projectGridOptions.data = result['data'];
                    $scope.projectGridOptions.columnDefs = cols;
                });
            $scope.generateProjectReport = function () {
                var from = '';
                var to = '';
                if (String($scope.resourceDate.fromDate).match(/[0-9]/)) {
                    from = new Date(Date.parse($scope.resourceDate.fromDate)).toLocaleDateString();
                }
                if (String($scope.resourceDate.toDate).match(/[0-9]/)) {
                    to = new Date(Date.parse($scope.resourceDate.toDate)).toLocaleDateString();
                }
                $http.get('/resource/report/generateTrendStatus?groupBy=project_name&returnType=chart&from=' + from + "&to=" + to + "&intervalGroup=" + $scope.resourceDate.intervalGroup)
                    .success(function (result) {
                        $scope.projectTrendColumnChartConfig.xAxis.categories = result.category;
                        $scope.projectTrendColumnChartConfig.series = result.series;
                    }).error(function (data, code) {
                    console.log(data);
                });
                $http.get('/resource/report/generateTrendStatus?groupBy=project_name,skill_name&from=' + from + "&to=" + to + "&intervalGroup=" + $scope.resourceDate.intervalGroup)
                    .success(function (result) {
                        var cols = [];
                        angular.forEach(result.col, function (arr) {
                            if (arr.name.match(/([0-9]|total)/i)) {
                                arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                                arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                    aggregation.rendered = aggregation.value.toFixed(2);
                                };
                            }
                            cols.push(arr);
                        });
                        $scope.projectSkillGridOptions.data = result['data'];
                        $scope.projectSkillGridOptions.columnDefs = cols;
                    });
                $http.get('/resource/report/generateTrendStatus?groupBy=project_name&from=' + from + "&to=" + to + "&intervalGroup=" + $scope.resourceDate.intervalGroup)
                    .success(function (result) {
                        var cols = [];
                        angular.forEach(result.col, function (arr) {
                            if (arr.name.match(/([0-9]|total)/i)) {
                                arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                                arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                    aggregation.rendered = aggregation.value.toFixed(2);
                                };
                            }
                            cols.push(arr);
                        });
                        $scope.projectGridOptions.data = result['data'];
                        $scope.projectGridOptions.columnDefs = cols;
                    });
            };
        }
        else if ($scope.page.match(/data$/i)) {
            $scope.dataGridOptions = {
                data: [],
                enableColumnMenus: false,
                enableGridMenu: true,
                exporterMenuPdf: false,
                exporterMenuCsv: false,
                treeRowHeaderAlwaysVisible: false,
                enableColumnResizing: true,
                enableFiltering: true,
                onRegisterApi: function (gridApi) {
                    $scope.gridApi = gridApi;
                },
                minRowsToShow: 20
            };
            $http.get('/resource/report/generateTrendStatus?groupBy=manager_name,employee_name')
                .success(function (result) {
                    var cols = [];
                    angular.forEach(result.col, function (arr) {
                        if (arr.name.match(/([0-9]|total)/i)) {
                            arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                            arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                aggregation.rendered = aggregation.value.toFixed(2);
                            };
                        }
                        cols.push(arr);
                    });
                    $scope.dataGridOptions.data = result['data'];
                    $scope.dataGridOptions.columnDefs = cols;
                });
            $scope.generateDataReport = function () {
                var from = '';
                var to = '';
                if (String($scope.resourceDate.fromDate).match(/[0-9]/)) {
                    from = new Date(Date.parse($scope.resourceDate.fromDate)).toLocaleDateString();
                }
                if (String($scope.resourceDate.toDate).match(/[0-9]/)) {
                    to = new Date(Date.parse($scope.resourceDate.toDate)).toLocaleDateString();
                }
                $http.get('/resource/report/generateTrendStatus?groupBy=manager_name,employee_name&from=' + from + "&to=" + to + "&intervalGroup=" + $scope.resourceDate.intervalGroup)
                    .success(function (result) {
                        var cols = [];
                        angular.forEach(result.col, function (arr) {
                            if (arr.name.match(/([0-9]|total)/i)) {
                                arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                                arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                    aggregation.rendered = aggregation.value.toFixed(2);
                                };
                            }
                            cols.push(arr);
                        });
                        $scope.dataGridOptions.data = result['data'];
                        $scope.dataGridOptions.columnDefs = cols;
                    });
            };
        }
        else if ($scope.page.match(/location/i)) {
            $scope.siteGridOptions = {
                data: [],
                enableColumnMenus: false,
                enableGridMenu: true,
                exporterMenuPdf: false,
                exporterMenuCsv: false,
                treeRowHeaderAlwaysVisible: false,
                enableColumnResizing: true,
                enableFiltering: true,
                onRegisterApi: function (gridApi) {
                    $scope.gridApi = gridApi;
                },
            };
            $http.get('/resource/report/generateTrendStatus?groupBy=design_center&returnType=chart')
                .success(function (result) {
                    $scope.locTrendColumnChartConfig.xAxis.categories = result.category;
                    $scope.locTrendColumnChartConfig.series = result.series;
                }).error(function (data, code) {
                console.log(data);
            });

            $http.get('/resource/report/generateTrendStatus?groupBy=design_center,project,skill')
                .success(function (result) {
                    var cols = [];
                    angular.forEach(result.col, function (arr) {
                        if (arr.name.match(/([0-9]|total)/i)) {
                            arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                            arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                aggregation.rendered = aggregation.value.toFixed(2);
                            };
                        }
                        cols.push(arr);
                    });
                    $scope.siteGridOptions.data = result['data'];
                    $scope.siteGridOptions.columnDefs = cols;
                });
            $scope.generateLocReport = function () {
                var from = '';
                var to = '';
                if (String($scope.resourceDate.fromDate).match(/[0-9]/)) {
                    from = new Date(Date.parse($scope.resourceDate.fromDate)).toLocaleDateString();
                }
                if (String($scope.resourceDate.toDate).match(/[0-9]/)) {
                    to = new Date(Date.parse($scope.resourceDate.toDate)).toLocaleDateString();
                }
                $http.get('/resource/report/generateTrendStatus?groupBy=design_center&returnType=chart&from=' + from + "&to=" + to)
                    .success(function (result) {
                        $scope.locTrendColumnChartConfig.xAxis.categories = result.category;
                        $scope.locTrendColumnChartConfig.series = result.series;
                    }).error(function (data, code) {
                    console.log(data);
                });

                $http.get('/resource/report/generateTrendStatus?groupBy=design_center,project,skill&from=' + from + "&to=" + to)
                    .success(function (result) {
                        var cols = [];
                        angular.forEach(result.col, function (arr) {
                            if (arr.name.match(/([0-9]|total)/i)) {
                                arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                                arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                    aggregation.rendered = aggregation.value.toFixed(2);
                                };
                            }
                            cols.push(arr);
                        });
                        $scope.siteGridOptions.data = result['data'];
                        $scope.siteGridOptions.columnDefs = cols;
                    });
            };
        }
    }

    $scope.locPieChartConfig = {
        credits: {
            enabled: false
        },
        options: {
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: 0,
                plotShadow: false
            }
        },
        func: function (chart) {
            $timeout(function () {
                chart.reflow();
            }, 1);
        },
        title: {
            text: "CSG Design Center Distribution"
        },
        subtitle: {},
        xAxis: {
            type: 'category',
            labels: {
                rotation: -45,
                style: {
                    fontSize: '13px',
                    fontFamily: 'Verdana, sans-serif'
                }
            }
        },
        yAxis: {
            title: {
                text: 'Headcount'
            },
            min: 0,
            allowDecimals: false
        },
        legend: {
            enabled: true,
            align: 'center',
        },
        tooltip: {
            crosshairs: true,
            shared: true,
            pointFormat: '{series.name}: <b>{point.y:.1f}</b>',
        },
        series: [{
            data: [],
            type: 'pie'
        }],
        exporting: {},
    };

    $scope.skillPieChartConfig = {
        credits: {
            enabled: false
        },
        options: {
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: 0,
                plotShadow: false
            }
        },
        func: function (chart) {
            $timeout(function () {
                chart.reflow();
            }, 1);
        },
        title: {
            text: "CSG Skill Distribution"
        },
        subtitle: {},
        xAxis: {
            type: 'category',
            labels: {
                rotation: -45,
                style: {
                    fontSize: '13px',
                    fontFamily: 'Verdana, sans-serif'
                }
            }
        },
        yAxis: {
            title: {
                text: 'Headcount'
            },
            min: 0,
            allowDecimals: false
        },
        legend: {
            enabled: true,
            align: 'center',
        },
        tooltip: {
            crosshairs: true,
            shared: true,
            pointFormat: '{series.name}: <b>{point.y:.1f}</b>',
        },
        series: [{
            data: [],
            type: 'pie'
        }],
        exporting: {},
    };

    $scope.projectPieChartConfig = {
        credits: {
            enabled: false
        },
        options: {
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: 0,
                plotShadow: false
            }
        },
        func: function (chart) {
            $timeout(function () {
                chart.reflow();
            }, 1);
        },
        title: {
            text: "CSG Project Distribution"
        },
        subtitle: {},
        xAxis: {
            type: 'category',
            labels: {
                rotation: -45,
                style: {
                    fontSize: '13px',
                    fontFamily: 'Verdana, sans-serif'
                }
            }
        },
        yAxis: {
            title: {
                text: 'Headcount'
            },
            min: 0,
            allowDecimals: false
        },
        legend: {
            enabled: true,
            align: 'center',
        },
        tooltip: {
            crosshairs: true,
            shared: true,
            pointFormat: '{series.name}: <b>{point.y:.1f}</b>',
        },
        series: [{
            data: [],
            type: 'pie'
        }],
        exporting: {},
    };

    $scope.locTrendColumnChartConfig = {
        credits: {
            enabled: false
        },
        options: {
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: 0,
                plotShadow: false,
                type: 'area'
            },
            plotOptions: {
                column: {stacking: 'normal'},
                area: {
                    stacking: 'normal',
                    lineColor: '#666666',
                    lineWidth: 1,
                    marker: {
                        lineWidth: 1,
                        lineColor: '#666666'
                    }
                }
            },
        },
        func: function (chart) {
            $timeout(function () {
                chart.reflow();
            }, 1);
        },
        title: {
            text: 'CSG Design Center Trend',
            align: 'center'
        },
        xAxis: {
            categories: []
        },
        yAxis: {
            title: {
                text: 'Headcount'
            },
            min: 0,
            allowDecimals: false
        },
        legend: {
            enabled: true,
            align: 'center',
        },
        tooltip: {
            crosshairs: true,
            shared: true
        },
        exporting: {},
        series: []
    };

    $scope.skillTrendColumnChartConfig = {
        credits: {
            enabled: false
        },
        options: {
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: 0,
                plotShadow: false,
                type: 'area'
            },
            plotOptions: {
                column: {stacking: 'normal'},
                area: {
                    stacking: 'normal',
                    lineColor: '#666666',
                    lineWidth: 1,
                    marker: {
                        lineWidth: 1,
                        lineColor: '#666666'
                    }
                }
            },
        },
        func: function (chart) {
            $timeout(function () {
                chart.reflow();
            }, 1);
        },
        title: {
            text: 'CSG Skill Trend',
            align: 'center'
        },
        xAxis: {
            categories: []
        },
        yAxis: {
            title: {
                text: 'Headcount'
            },
            min: 0,
            allowDecimals: false
        },
        legend: {
            enabled: true,
            align: 'center',
            y: 100
        },
        tooltip: {
            crosshairs: true,
            shared: true
        },
        exporting: {},
        series: []
    };

    $scope.projectTrendColumnChartConfig = {
        credits: {
            enabled: false
        },
        options: {
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: 0,
                plotShadow: false,
                type: 'area'
            },
            plotOptions: {
                column: {stacking: 'normal'},
                area: {
                    stacking: 'normal',
                    lineColor: '#666666',
                    lineWidth: 1,
                    marker: {
                        lineWidth: 1,
                        lineColor: '#666666'
                    }
                }
            },
        },
        func: function (chart) {
            $timeout(function () {
                chart.reflow();
            }, 1);
        },
        title: {
            text: 'CSG Project Trend',
            align: 'center'
        },
        xAxis: {
            categories: [],
            tickmarkPlacement: 'on',
            title: {
                enabled: false
            }
        },
        yAxis: {
            title: {
                text: 'Headcount'
            },
            min: 0,
            allowDecimals: false
        },
        legend: {
            enabled: false,
            align: 'center'
        },
        tooltip: {
            crosshairs: true,
            split: true
        },
        exporting: {},
        series: []
    };

});