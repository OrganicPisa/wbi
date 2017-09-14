App.controller('SoftwareProgramCtrl', function ($scope, $rootScope, $http, $sce, $stateParams, $filter, $location,
                                                $localStorage, Notification, $mdDialog, $mdSidenav,
                                                infoFactory, contactFactory, swFactory,
                                                linkFactory, skuFactory, headlineFactory,
                                                outlookFactory, milestoneFactory, remarkFactory, authenticationFactory) {
    $scope.pid = $stateParams.pid;
    $scope.rid = $stateParams.rid;
    $scope.revisions = [];
    $scope.page = $stateParams.page;
    $scope.stage = '';
    $scope.trustAsHtml = $sce.trustAsHtml;

    $scope.headline = {};
    $scope.headline.snapshots = [];
    $scope.snapshotHeadline = false;
    $scope.headline.snapshotSelect = '';
    $scope.infoDashboard = [];
    $scope.currentRevision = {};
    $scope.rname = '';
    $scope.programName = '';
    $scope.alerts = [];

    $scope.meetings = [];
    $scope.links = [];
    $scope.outlook = "";
    $scope.outlookTimestamp = "";
    $scope.resources = [];
    $scope.resourceChart = [];
    $scope.resourceTimeArray = [];

    $scope.isCustomerProgram = false;
    $scope.isChipProgram = false;

    $scope.swo = {};
    $scope.swo.predicate1 = 'order';
    $scope.swo.predicate2 = 'rname';
    $scope.swo.inReport = true;
    $scope.currentswtab = '';

    $scope.openSideMenu = function () {
        $mdSidenav('left').toggle()
    };

    if ($rootScope.displaySwReportBtn) {
        $scope.swo.inReport = $rootScope.displaySwReportBtn;
    }
    $http.get("/api/segment/getPrograms?segment=SOFTWARE&type=software&status=true")
        .then(function (result) {
            $scope.swtabs = $filter('orderBy')(result.data, 'reportName');
            var filtered = $filter('filter')($scope.swtabs, {pid: $scope.pid});
            if (typeof filtered != 'undefined') {
                var currentTab = filtered[0];
                $scope.currentswtab = currentTab.reportName;
                $scope.swtabIndex = $scope.swtabs.indexOf(currentTab);
            }
        });
    $scope.loadSWTab = function (tab) {
        if ($scope.pid != tab.pid) {
            $scope.pid = tab.pid;
            $scope.currentswtab = tab.program;
        }
    };
    $scope.$watch('swo.inReport', function (val) {
        if (val) {
            $scope.sws = $filter('filter')($scope.allsws, {includeReport: 'true'});
            $rootScope.displaySwReportBtn = true;
        }
        else {
            $scope.sws = $scope.allsws;
            $rootScope.displaySwReportBtn = false;
        }
    });
    $scope.refreshsw = function () {
        swFactory.get($scope.pid, 1)
            .then(function (result) {
                if (result) {
                    $scope.allsws = result;
                    if ($scope.swo.inReport) {
                        $scope.sws = $filter('filter')($scope.allsws, {includeReport: 'true'});
                    }
                    else {
                        $scope.sws = $scope.allsws;
                    }
                }
            }, function (data, code) {
                Notification.error(data);
            });
    };
    $scope.$watch('pid', function (val) {
        swFactory.get($scope.pid, 0)
            .then(function (result) {
                if (result) {
                    $scope.allsws = result.data;
                    if ($scope.swo.inReport) {
                        $scope.sws = $filter('filter')($scope.allsws, {includeReport: 'true'});
                    }
                    else {
                        $scope.sws = $scope.allsws;
                    }
                }
            }, function (data, code) {
                Notification.error(data);
            });
    });
    $scope.updateSWStatus = function (sw, color) {
        sw.color = color;
    };
    $scope.filterSw = function (sw) {
        return swFactory.filter(sw);
    };
    $scope.addNewSw = function (obj) {
        obj = swFactory.add(obj);
    };
    $scope.cancelSaveSw = function () {
        $scope.sws = swFactory.cancel($scope.sws);
    };
    $scope.saveSwTable = function () {
        swFactory.save($scope.pid, $scope.sws, function () {
            swFactory.get($scope.pid, 1)
                .then(function (result) {
                    if (result) {
                        $scope.allsws = result.data;
                        if ($scope.swo.inReport) {
                            $scope.sws = $filter('filter')($scope.allsws, {includeReport: 'true'});
                        }
                        else {
                            $scope.sws = $scope.allsws;
                        }
                    }
                }, function (data, code) {
                    Notification.error(data);
                });
        });
    };

});