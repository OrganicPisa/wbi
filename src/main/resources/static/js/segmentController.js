App.controller('HomeCtrl', function ($scope, $rootScope, $log, $filter, $http, $sce, $interval, $timeout,
                                     $stateParams, $location, $localStorage, $interval,
                                     Notification, uiGridConstants) {
    $scope.tabs = [];
    $scope.ptype = "chip";
    $scope.status = $stateParams.status.replace(/^(home|index|active)/i, "true").replace(/archived/i, "false");
    $scope.rowCollection = [];
    $scope.displayCollection = [];
    $scope.selectedIndex = -1;
    $scope.frontpageDisplayBookmarkOnly = false;
    $scope.selectedTab = {};

    $scope.trustAsHtml = $sce.trustAsHtml;

    if (angular.isDefined($localStorage.frontpageDisplayBookmarkOnly)) {
        $scope.frontpageDisplayBookmarkOnly = $localStorage.frontpageDisplayBookmarkOnly;
    }
    else {
        $scope.frontpageDisplayBookmarkOnly = false;
    }

    function saveState() {
        var state = $scope.gridApi.saveState.save();
        $localStorage.frontPageIPGridState = state;
    }

    function restoreState() {
        $timeout(function () {
            var state = $localStorage.frontPageIPGridState;
            if (state) $scope.gridApi.saveState.restore($scope, state);
        });
    }

    $scope.gridOptions = {
        enableGridMenu: true,
        exporterMenuPdf: false,
        enableColumnResizing: true,
        exporterCsvFilename: 'ip_dashboard.csv',
        enableFiltering: true,
        columnDefs: [],
        minRowsToShow: 15,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            $scope.gridApi.colMovable.on.columnPositionChanged($scope, saveState);
            $scope.gridApi.colResizable.on.columnSizeChanged($scope, saveState);
            $scope.gridApi.core.on.columnVisibilityChanged($scope, saveState);
            $scope.gridApi.core.on.filterChanged($scope, saveState);
            $scope.gridApi.core.on.sortChanged($scope, saveState);
            restoreState();
        }
    };
    var ipTableColumnDef = [
        {
            field: 'schedule_flag', name: 'I', width: '5%', enableFiltering: false,
            cellClass: function (grid, row, col, rowRenderIndex, colRenderIndex) {
                var color = "normal";
                if (grid.getCellValue(row, col).toLowerCase().match(/(delay|red)/i)) {

                    color = 'delay';
                }
                else if (grid.getCellValue(row, col).toLowerCase().match(/(risk|orange)/i)) {
                    color = 'risk';
                }
                return color;
            }
        },
        {
            field: 'stage', filter: {
            type: uiGridConstants.filter.SELECT,
            selectOptions: []
        }
        },
        {
            field: 'technology', filter: {
            type: uiGridConstants.filter.SELECT,
            selectOptions: []
        }
        },
        {
            field: 'category', filter: {
            type: uiGridConstants.filter.SELECT,
            selectOptions: []
        }
        },
        {
            field: 'dft', filter: {
            type: uiGridConstants.filter.SELECT,
            selectOptions: []
        }
        },
        {field: 'program'},
        {field: 'rev', cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"},
        {
            field: 'chip usage',
            name: 'Chips + (# instances)',
            cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
        },
        {
            field: 'type',
            visible: false,
            cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
        },
        {
            field: 'area',
            visible: false,
            cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
        },
        {
            field: 'dimension',
            visible: false,
            cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
        },
        {
            field: 'power',
            visible: false,
            cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
        },
        {
            field: 'owner',
            visible: false,
            cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
        },
        {
            field: 'date preview',
            visible: false,
            cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
        },
        {
            field: 'gds final',
            visible: false,
            cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
        },
        {
            field: 'headline',
            visible: false,
            cellTemplate: "<div style='padding:5px;' dynamic='row.entity[col.field]'></div>"
        }
    ];

    if ('category' in $rootScope.ipDropDownTemplate) {
        var cat = $filter('filter')(ipTableColumnDef, {field: 'category'});
        if (cat.length > 0) {
            angular.forEach($rootScope.ipDropDownTemplate.category, function (value, key) {
                var obj = {};
                obj.value = value;
                obj.label = key;
                cat[0].filter.selectOptions.push(obj);
            });
        }
    }
    if ('technology' in $rootScope.ipDropDownTemplate) {
        var tech = $filter('filter')(ipTableColumnDef, {'field': 'technology'});
        if (tech.length > 0) {
            angular.forEach($rootScope.ipDropDownTemplate.technology, function (value, key) {
                var obj = {};
                obj.value = value;
                obj.label = key;
                tech[0].filter.selectOptions.push(obj);
            });
        }
    }
    if ('dft' in $rootScope.ipDropDownTemplate) {
        var dft = $filter('filter')(ipTableColumnDef, {'field': 'dft'});
        if (dft.length > 0) {
            angular.forEach($rootScope.ipDropDownTemplate.dft, function (value, key) {
                var obj = {};
                obj.value = value;
                obj.label = key;
                dft[0].filter.selectOptions.push(obj);
            });
        }
    }
    if ('stage' in $rootScope.ipDropDownTemplate) {
        var stage = $filter('filter')(ipTableColumnDef, {'field': 'stage'});
        if (stage.length > 0) {
            angular.forEach($rootScope.ipDropDownTemplate.stage, function (value, key) {
                var obj = {};
                obj.value = value;
                obj.label = key;
                stage[0].filter.selectOptions.push(obj);
            });
        }
    }
    $scope.gridOptions.columnDefs = ipTableColumnDef;

    $http.get("/api/segment/getActiveSegments")
        .then(function (result) {
            $scope.tabs = $filter('orderBy')(result.data, 'orderNum');
            if (angular.isDefined($localStorage.currentDashboardSegmentClicked)) {
                $scope.selectedIndex = $localStorage.currentDashboardSegmentClicked;
            }
            else{
                $scope.selectedIndex = 0;
            }
            $scope.selectedTab = $scope.tabs[$scope.selectedIndex];
        }, function (data, status) {
            Notification.error({message: data, title: 'Error', replaceMessage: true});
        });

    $scope.loadTab = function (tab) {
        $scope.selectedTab = tab;
    };
    $scope.refreshTab = function () {
        $http.get("/api/segment/getPrograms?segment=" + $scope.selectedTab.name + "&status=" + $scope.status + "&reload=1")
            .then(function (result) {
                $scope.rowCollection = [];
                $scope.displayCollection = [];
                if (typeof result.data != 'undefined') {
                    if (!$scope.ptype.match(/^ip/i)) {
                        if (typeof result.data == 'string' && result.data.match(/[0-9a-zA-Z]/)) {
                            $scope.rowCollection = angular.fromJson($filter('orderBy')(result.data, ['pid', 'rid']));
                        }
                        else if (typeof result.data != 'string') {
                            $scope.rowCollection = angular.fromJson($filter('orderBy')(result.data, ['pid', 'rid']));
                        }
                        if ($scope.rowCollection.length > 0) {
                            $scope.displayCollection = [].concat($scope.rowCollection);
                            if ($scope.frontpageDisplayBookmarkOnly) {
                                $scope.displayCollection = $filter('filter')($scope.rowCollection, {bookmark: true});
                            }
                        }
                    }
                    else {
                        var arr = result.data;
                        var rlength = arr.length + 1;
                        if (rlength > 23)
                            rlength = 23;
                        $scope.gridOptions.minRowsToShow = rlength;
                        $scope.gridOptions.data = result.data;
                    }
                }
            }, function (data, status) {
                Notification.error({message: data, title: 'Error', replaceMessage: true});
            });
    };
    // $scope.$watch('segment', function (val){
    $scope.$watch('selectedTab', function(tab){
        if(typeof tab.name != 'undefined') {
            $scope.selectedIndex = $scope.tabs.indexOf(tab);
            $localStorage.currentDashboardSegmentClicked = $scope.selectedIndex;
            if (tab.name.match(/^customer/i)) {
                $scope.ptype = "Customer";
            }
            else if (tab.name.match(/^software/i)) {
                $scope.ptype = "SW";
            }
            else if (tab.name.match(/^ip/i)) {
                $scope.ptype = "ip";
            }
            else {
                $scope.ptype = "Internal";
            }
            $scope.displayCollection = [];
            $http.get("/api/segment/getPrograms?segment=" + tab.name + "&status=" + $scope.status)
                .then(function (result) {
                    $scope.rowCollection = [];
                    $scope.displayCollection = [];
                    if (typeof result != 'undefined') {
                        if (!$scope.ptype.match(/^ip/i)) {
                            if (typeof result.data == 'string' && result.match(/[0-9a-zA-Z]/)) {
                                $scope.rowCollection = angular.fromJson($filter('orderBy')(result.data, ['pid', 'rid']));
                            }
                            else if (typeof result.data != 'string') {
                                $scope.rowCollection = angular.fromJson($filter('orderBy')(result.data, ['pid', 'rid']));
                            }
                            if ($scope.rowCollection.length > 0) {
                                $scope.displayCollection = [].concat($scope.rowCollection);
                                if ($scope.frontpageDisplayBookmarkOnly) {
                                    $scope.displayCollection = $filter('filter')($scope.rowCollection, {bookmark: true});
                                }
                            }
                        }
                        else {
                            var arr = result.data;
                            var rlength = arr.length + 1;
                            if (rlength > 23)
                                rlength = 23;
                            $scope.gridOptions.minRowsToShow = rlength;
                            $scope.gridOptions.data = result.data;
                        }
                    }
                }, function (data, status) {
                    Notification.error({message: data, title: 'Error', replaceMessage: true});
                });
        }
    });
    $scope.$watch('frontpageDisplayBookmarkOnly', function (value) {
        if (value) {
            $localStorage.frontpageDisplayBookmarkOnly = true;
        }
        else {
            $localStorage.frontpageDisplayBookmarkOnly = false;
        }
    });

    $scope.saveBookmark = function (row) {
        $http.post('/api/revision/saveBookmark?rid=' + row.rid + "&bookmark=" + !row.bookmark)
    };
});