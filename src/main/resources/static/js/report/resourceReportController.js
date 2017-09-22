Array.prototype.pushArray = function (arr) {
    this.push.apply(this, arr);
};
App.controller('ResourceReportCtrl', function ($scope, $rootScope, $filter, $http, $sce, $timeout, $stateParams,
                                               $location, $window, $interval, uiGridConstants, $mdDialog, $mdSidenav,
                                               $localStorage, uiGridGroupingConstants, Notification) {
    $scope.page = $stateParams.page;
    var chart;

    (function (H) {
        H.wrap(H.Chart.prototype.initReflow = function () {
            var chart = this,
                reflow = function (e) {
                    if (chart && chart.options) {
                        chart.reflow(e);
                    }
                };


            H.addEvent(window, 'resize', reflow);
            H.addEvent(chart, 'destroy', function () {
                H.removeEvent(window, 'resize', reflow);
            });
        });
    })(Highcharts);

    $scope.go = function (url) {
        $location.path(url);
    };
    $scope.openSideMenu = function () {
        $mdSidenav('left').toggle()
    };

    $scope.syncResourceData = function () {
        $http.post("/api/resource/sync");
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
        $http.post('/api/resource/report/clearCache').then(function (ret) {
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
        $http.get('/api/resource/report/generateCurrentMonthStatus?groupBy=design_center')
            .then(function (result) {
                $scope.locPieChartConfig.series[0].data = result.data;
            }, function (data, code) {
                console.log(data);
            });
        $http.get('/api/resource/report/generateCurrentMonthStatus?groupBy=skill_name')
            .then(function (result) {
                $scope.skillPieChartConfig.series[0].data = result.data;
            }, function (data, code) {
                console.log(data);
            });
        $http.get('/api/resource/report/generateCurrentMonthStatus?groupBy=project_name')
            .then(function (result) {
                $scope.projectPieChartConfig.series[0].data = result.data;
            }, function (data, code) {
                console.log(data);
            });

        $http.get('/api/resource/report/generateTrendStatus?groupBy=design_center&returnType=chart')
            .then(function (result) {
                $scope.locTrendColumnChartConfig.xAxis.categories = result.data.category;
                $scope.locTrendColumnChartConfig.series = result.data.series;
            }, function (data, code) {
                console.log(data);
            });
        $http.get('/api/resource/report/generateTrendStatus?groupBy=skill_name&returnType=chart')
            .then(function (result) {
                $scope.skillTrendColumnChartConfig.xAxis.categories = result.data.category;
                $scope.skillTrendColumnChartConfig.series = result.data.series;
            }, function (data, code) {
                console.log(data);
            });
        $http.get('/api/resource/report/generateTrendStatus?groupBy=project_name&returnType=chart')
            .then(function (result) {
                $scope.projectTrendColumnChartConfig.xAxis.categories = result.data.category;
                $scope.projectTrendColumnChartConfig.series = result.data.series;
            }, function (data, code) {
                console.log(data);
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

        $http.get('/api/resource/report/getAllProjectGroup')
            .then(function (result) {
                $scope.rs.type_list = result.data;
            });

        $http.get('/api/resource/report/getDistinctValue?type=project_name')
            .then(function (result) {
                $scope.rs.all_project_list = result.data;
            }, function (data, code) {
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
            $http.post("/api/resource/report/saveProjectClassificationList", {
                "id": $scope.rs.exist.id,
                "name": $scope.rs.exist.name,
                "list": $scope.rs.exist.project_list,
                "status": $scope.rs.exist.includeInReport
            })
                .then(function (ret) {
                    $http.get('/api/resource/report/getDistinctProjectByType?id=' + $scope.rs.exist.id)
                        .then(function (result) {
                            $scope.rs.exist.project_list = result.data.projects;
                            $scope.rs.exist.includeInReport = result.data.inReport;
                            $scope.rs.exist.type = result.data.type;
                        }, function (data, code) {
                            console.log(data);
                        });
                });
        };
        $scope.deleteResourceProjectSettings = function () {
            $http.post("/api/resource/report/deleteProjectClassificationList", {"id": $scope.rs.exist.id})
                .then(function (ret) {
                    $http.get('/api/resource/report/getAllProjectGroup')
                        .then(function (result) {
                            $scope.rs.type_list = result.data;
                        });
                });
        };
        $scope.newResourceProjectSettings = function () {
            $http.post("/api/resource/report/saveProjectClassificationList", {
                "id": 0,
                "name": $scope.rs.newgroup.name,
                "type": $scope.rs.newgroup.type,
                "status": $scope.rs.newgroup.includeInReport
            })
                .then(function (ret) {
                    $http.get('/api/resource/report/getAllProjectGroup')
                        .then(function (result) {
                            $scope.rs.type_list = result.data;
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
        else if ($scope.page.match(/projectskill/i))
            groupType = "project,skill";
        $scope.rs = {};
        $scope.rs.filterChargedFrom = "None";
        $scope.rs.employeeType = "all";
        $scope.monthlyGridOptions = {
            data: [],
            enableColumnMenus: false,
            enableGridMenu: true,
            exporterMenuPdf: false,
            exporterMenuCsv: true,
            enableColumnResizing: true,
            enableFiltering: true,
            onRegisterApi: function (gridApi) {
                $scope.gridApi = gridApi;
            },
            minRowsToShow: 50
        };
        $http.get("/api/resource/report/generateProjectTrendStatus?intervalGroup=" + $scope.resourceDate.intervalGroup +
            "&type=" + groupType.toLowerCase() + "&filter=" + $scope.rs.filterChargedFrom + "&employeeType=" + $scope.rs.employeeType)
            .then(function (result) {
                var cols = [];
                angular.forEach(result.data.col, function (arr) {
                    cols.push(arr);
                });
                $scope.monthlyGridOptions.columnDefs = cols;
                $scope.monthlyGridOptions.data = result.data.data;
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
            $http.get("/api/resource/report/generateProjectTrendStatus?from=" + from + "&to=" + to + "&intervalGroup=" + $scope.resourceDate.intervalGroup +
                "&type=" + groupType.toLowerCase() + "&filter=" + $scope.rs.filterChargedFrom + "&employeeType=" + $scope.rs.employeeType)
                .then(function (result) {
                    var cols = [];
                    angular.forEach(result.data.col, function (arr) {
                        cols.push(arr);
                    });
                    $scope.monthlyGridOptions.columnDefs = cols;
                    $scope.monthlyGridOptions.data = result.data.data;
                });
        };
    }
    else if ($scope.page.match(/skill/i)) {
        $scope.rs = {};
        $scope.rs.filterChargedFrom = "None";
        $scope.rs.employeeType = "all";
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
        $http.get('/api/resource/report/generateTrendStatus?groupBy=skill_name&returnType=chart' +
            "&filter=" + $scope.rs.filterChargedFrom + "&employeeType=" + $scope.rs.employeeType)
            .then(function (result) {
                $scope.skillTrendColumnChartConfig.xAxis.categories = result.data.category;
                $scope.skillTrendColumnChartConfig.series = result.data.series;
            }, function (data, code) {
                console.log(data);
            });
        $http.get('/api/resource/report/generateTrendStatus?groupBy=skill_name&returnType=table' +
            "&filter=" + $scope.rs.filterChargedFrom + "&employeeType=" + $scope.rs.employeeType)
            .then(function (result) {
                $scope.skillGridOptions.data = result.data.data;
                $scope.skillGridOptions.columnDefs = result.data.col;
            }, function (data, code) {
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
            $http.get('/api/resource/report/generateTrendStatus?groupBy=skill_name&returnType=chart&from=' + from + "&to=" + to + "&intervalGroup=" + $scope.resourceDate.intervalGroup +
                "&filter=" + $scope.rs.filterChargedFrom + "&employeeType=" + $scope.rs.employeeType)
                .then(function (result) {
                    $scope.skillTrendColumnChartConfig.xAxis.categories = result.data.category;
                    $scope.skillTrendColumnChartConfig.series = result.data.series;
                }, function (data, code) {
                    console.log(data);
                });
            $http.get('/api/resource/report/generateTrendStatus?groupBy=skill_name&returnType=table&from=' + from + "&to=" + to +
                "&filter=" + $scope.rs.filterChargedFrom + "&employeeType=" + $scope.rs.employeeType)
                .then(function (result) {
                    $scope.skillGridOptions.data = result.data.data;
                    $scope.skillGridOptions.columnDefs = result.data.col;
                }, function (data, code) {
                    console.log(data);
                });
        };
    }
    else if ($scope.page.match(/project$/i)) {
        $scope.rs = {};
        $scope.rs.filterChargedFrom = "None";
        $scope.rs.employeeType = "all";
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
        $http.get('/api/resource/report/generateTrendStatus?groupBy=project_name&returnType=chart' +
            "&filter=" + $scope.rs.filterChargedFrom + "&employeeType=" + $scope.rs.employeeType)
            .then(function (result) {
                $scope.projectTrendColumnChartConfig.xAxis.categories = result.data.category;
                $scope.projectTrendColumnChartConfig.series = result.data.series;
            }, function (data, code) {
                console.log(data);
            });
        $http.get('/api/resource/report/generateTrendStatus?groupBy=project_name,skill_name' +
            "&filter=" + $scope.rs.filterChargedFrom + "&employeeType=" + $scope.rs.employeeType)
            .then(function (result) {
                var cols = [];
                angular.forEach(result.data.col, function (arr) {
                    if (arr.name.match(/([0-9]|total)/i)) {
                        arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                        arr.customTreeAggregationFinalizerFn = function (aggregation) {
                            aggregation.rendered = aggregation.value.toFixed(2);
                        };
                    }
                    cols.push(arr);
                });
                $scope.projectSkillGridOptions.data = result.data.data;
                $scope.projectSkillGridOptions.columnDefs = cols;
            });
        $http.get('/api/resource/report/generateTrendStatus?groupBy=project_name' +
            "&filter=" + $scope.rs.filterChargedFrom + "&employeeType=" + $scope.rs.employeeType)
            .then(function (result) {
                var cols = [];
                angular.forEach(result.data.col, function (arr) {
                    if (arr.name.match(/([0-9]|total)/i)) {
                        arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                        arr.customTreeAggregationFinalizerFn = function (aggregation) {
                            aggregation.rendered = aggregation.value.toFixed(2);
                        };
                    }
                    cols.push(arr);
                });
                $scope.projectGridOptions.data = result.data.data;
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
            $http.get('/api/resource/report/generateTrendStatus?groupBy=project_name&returnType=chart&from=' + from + "&to=" + to + "&intervalGroup=" + $scope.resourceDate.intervalGroup +
                "&filter=" + $scope.rs.filterChargedFrom + "&employeeType=" + $scope.rs.employeeType)
                .then(function (result) {
                    $scope.projectTrendColumnChartConfig.xAxis.categories = result.data.category;
                    $scope.projectTrendColumnChartConfig.series = result.data.series;
                }, function (data, code) {
                    console.log(data);
                });
            $http.get('/api/resource/report/generateTrendStatus?groupBy=project_name,skill_name&from=' + from + "&to=" + to + "&intervalGroup=" + $scope.resourceDate.intervalGroup +
                "&filter=" + $scope.rs.filterChargedFrom + "&employeeType=" + $scope.rs.employeeType)
                .then(function (result) {
                    var cols = [];
                    angular.forEach(result.data.col, function (arr) {
                        if (arr.name.match(/([0-9]|total)/i)) {
                            arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                            arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                aggregation.rendered = aggregation.value.toFixed(2);
                            };
                        }
                        cols.push(arr);
                    });
                    $scope.projectSkillGridOptions.data = result.data.data;
                    $scope.projectSkillGridOptions.columnDefs = cols;
                });
            $http.get('/api/resource/report/generateTrendStatus?groupBy=project_name&from=' + from + "&to=" + to + "&intervalGroup=" + $scope.resourceDate.intervalGroup +
                "&filter=" + $scope.rs.filterChargedFrom + "&employeeType=" + $scope.rs.employeeType)
                .then(function (result) {
                    var cols = [];
                    angular.forEach(result.data.col, function (arr) {
                        if (arr.name.match(/([0-9]|total)/i)) {
                            arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                            arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                aggregation.rendered = aggregation.value.toFixed(2);
                            };
                        }
                        cols.push(arr);
                    });
                    $scope.projectGridOptions.data = result.data.data;
                    $scope.projectGridOptions.columnDefs = cols;
                });
        };
    }
    else if ($scope.page.match(/manager$/i)) {
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
        $http.get('/api/resource/report/generateTrendStatus?groupBy=manager_name,employee_name')
            .then(function (result) {
                var cols = [];
                angular.forEach(result.data.col, function (arr) {
                    if (arr.name.match(/([0-9]|total)/i)) {
                        arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                        arr.customTreeAggregationFinalizerFn = function (aggregation) {
                            aggregation.rendered = aggregation.value.toFixed(2);
                        };
                    }
                    cols.push(arr);
                });
                $scope.dataGridOptions.data = result.data.data;
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
            $http.get('/api/resource/report/generateTrendStatus?groupBy=manager_name,employee_name&from=' + from + "&to=" + to + "&intervalGroup=month")
                .then(function (result) {
                    var cols = [];
                    angular.forEach(result.data.col, function (arr) {
                        if (arr.name.match(/([0-9]|total)/i)) {
                            arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
                            arr.customTreeAggregationFinalizerFn = function (aggregation) {
                                aggregation.rendered = aggregation.value.toFixed(2);
                            };
                        }
                        cols.push(arr);
                    });
                    $scope.dataGridOptions.data = result.data.data;
                    $scope.dataGridOptions.columnDefs = cols;
                });
        };
    }
    // else if ($scope.page.match(/location/i)) {
    //     $scope.siteGridOptions = {
    //         data: [],
    //         enableColumnMenus: false,
    //         enableGridMenu: true,
    //         exporterMenuPdf: false,
    //         exporterMenuCsv: false,
    //         treeRowHeaderAlwaysVisible: false,
    //         enableColumnResizing: true,
    //         enableFiltering: true,
    //         onRegisterApi: function (gridApi) {
    //             $scope.gridApi = gridApi;
    //         },
    //     };
    //     $http.get('/resource/report/generateTrendStatus?groupBy=design_center&returnType=chart')
    //         .success(function (result) {
    //             $scope.locTrendColumnChartConfig.xAxis.categories = result.category;
    //             $scope.locTrendColumnChartConfig.series = result.series;
    //         }).error(function (data, code) {
    //         console.log(data);
    //     });
    //
    //     $http.get('/resource/report/generateTrendStatus?groupBy=design_center,project,skill')
    //         .success(function (result) {
    //             var cols = [];
    //             angular.forEach(result.col, function (arr) {
    //                 if (arr.name.match(/([0-9]|total)/i)) {
    //                     arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
    //                     arr.customTreeAggregationFinalizerFn = function (aggregation) {
    //                         aggregation.rendered = aggregation.value.toFixed(2);
    //                     };
    //                 }
    //                 cols.push(arr);
    //             });
    //             $scope.siteGridOptions.data = result['data'];
    //             $scope.siteGridOptions.columnDefs = cols;
    //         });
    //     $scope.generateLocReport = function () {
    //         var from = '';
    //         var to = '';
    //         if (String($scope.resourceDate.fromDate).match(/[0-9]/)) {
    //             from = new Date(Date.parse($scope.resourceDate.fromDate)).toLocaleDateString();
    //         }
    //         if (String($scope.resourceDate.toDate).match(/[0-9]/)) {
    //             to = new Date(Date.parse($scope.resourceDate.toDate)).toLocaleDateString();
    //         }
    //         $http.get('/resource/report/generateTrendStatus?groupBy=design_center&returnType=chart&from=' + from + "&to=" + to)
    //             .success(function (result) {
    //                 $scope.locTrendColumnChartConfig.xAxis.categories = result.category;
    //                 $scope.locTrendColumnChartConfig.series = result.series;
    //             }).error(function (data, code) {
    //             console.log(data);
    //         });
    //
    //         $http.get('/resource/report/generateTrendStatus?groupBy=design_center,project,skill&from=' + from + "&to=" + to)
    //             .success(function (result) {
    //                 var cols = [];
    //                 angular.forEach(result.col, function (arr) {
    //                     if (arr.name.match(/([0-9]|total)/i)) {
    //                         arr.treeAggregationType = uiGridGroupingConstants.aggregation.SUM;
    //                         arr.customTreeAggregationFinalizerFn = function (aggregation) {
    //                             aggregation.rendered = aggregation.value.toFixed(2);
    //                         };
    //                     }
    //                     cols.push(arr);
    //                 });
    //                 $scope.siteGridOptions.data = result['data'];
    //                 $scope.siteGridOptions.columnDefs = cols;
    //             });
    //     };
    // }

    // $scope.locPieChartConfig = {
    //     credits: {
    //         enabled: false
    //     },
    //     options: {
    //         chart: {
    //             plotBackgroundColor: null,
    //             plotBorderWidth: 0,
    //             plotShadow: false
    //         }
    //     },
    //     func: function (chart) {
    //         $timeout(function () {
    //             chart.reflow();
    //         }, 1);
    //     },
    //     title: {
    //         text: "CSG Design Center Distribution"
    //     },
    //     subtitle: {},
    //     xAxis: {
    //         type: 'category',
    //         labels: {
    //             rotation: -45,
    //             style: {
    //                 fontSize: '13px',
    //                 fontFamily: 'Verdana, sans-serif'
    //             }
    //         }
    //     },
    //     yAxis: {
    //         title: {
    //             text: 'Headcount'
    //         },
    //         min: 0,
    //         allowDecimals: false
    //     },
    //     legend: {
    //         enabled: true,
    //         align: 'center',
    //     },
    //     tooltip: {
    //         crosshairs: true,
    //         shared: true,
    //         pointFormat: '{series.name}: <b>{point.y:.1f}</b>',
    //     },
    //     series: [{
    //         data: [],
    //         type: 'pie'
    //     }],
    //     exporting: {},
    // };

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
            chart.reflow();
        },
        title: {text: ''},
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
            chart.reflow();
        },
        title: {text: ''},
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
            chart.reflow();
        },
        title: {text: ''},
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
            chart.reflow();
        },
        title: {text: ''},
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