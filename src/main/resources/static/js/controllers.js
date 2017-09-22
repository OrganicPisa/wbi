App.controller('HomeCtrl', function ($scope, $log, $filter, $http, $sce, $timeout, $stateParams, Notification) {
    $scope.tabs = [];
    $scope.segmentloaded = false;
    $scope.bookmarkdisplay = false;
    $scope.dataloaded = false;
    $scope.ptype = "chip";
    $scope.status = $stateParams.status.replace(/^(home|index|active)/i, "true").replace(/archived/i, "false");
    $scope.segment = '';
    $scope.rowCollection = [];
    $scope.displayCollection = [];
    $scope.tabIndex = 0;

    $scope.trustAsHtml = $sce.trustAsHtml;

    if (angular.isDefined($scope.wbi.displayProjectBookmarkBtn)) {
        $scope.bookmarkdisplay = $scope.wbi.displayProjectBookmarkBtn;
    }

    $http.get("/api/segment/getActiveSegments")
        .then(function (result) {
            $scope.tabs = result;
            $scope.segmentloaded = true;
            if (angular.isDefined($scope.wbi.currentSegmentClicked)) {
                $scope.segment = $scope.wbi.currentSegmentClicked;
                var currenttab = $filter('filter')($scope.tabs, {name: $scope.segment.toUpperCase()})[0];
                $scope.tabIndex = $scope.tabs.indexOf(currenttab);
            }
        }, function (data, status) {
            $scope.segmentloaded = true;
            Notification.error({message: data, title: 'Error', replaceMessage: true})
        });
    $scope.loadTab = function (tab) {
        $scope.tabIndex = $scope.tabs.indexOf(tab);
        if ($scope.segment.localeCompare(tab.name) != 0) {
            $scope.segment = tab.name;
        }
    };
    $scope.$watch('segment', function (val) {
        if (val.match(/[a-zA-Z]/)) {
            $scope.dataloaded = false;
            Notification.info({message: 'Please Wait...', replaceMessage: true});
            $scope.wbi.currentSegmentClicked = val;
            if (val.match(/customer/i)) {
                $scope.ptype = "Customer";
            }
            else if (val.match(/software/i)) {
                $scope.ptype = "SW";
            }
            else {
                $scope.ptype = "Internal";
            }
            $scope.wbi.currentSegmentClicked = val;
            $scope.searchquery = '';
            $scope.displayCollection = [];
            $http.get("/api/segment/getPrograms?segment=" + $scope.segment + "&status=" + $scope.status)
                .then(function (result) {
                    $scope.rowCollection = [];
                    $scope.displayCollection = [];
                    if (typeof result != 'undefined') {
                        if (typeof result == 'string' && result.match(/[0-9a-zA-Z]/)) {
                            $scope.rowCollection = angular.fromJson(result);
                        }
                        else if (typeof result != 'string') {
                            $scope.rowCollection = angular.fromJson(result);
                        }
                        if ($scope.rowCollection.length > 0) {
                            $scope.displayCollection = [].concat($scope.rowCollection);
                            if ($scope.bookmarkdisplay) {
                                $scope.displayCollection = $filter('filter')($scope.rowCollection, {bookmark: true});
                            }
                        }
                    }
                    $scope.dataloaded = true;
                }, function (data, status) {
                    $scope.dataloaded = true;
                    Notification.error({message: data, title: 'Error', replaceMessage: true});
                });
        }
    });
    $scope.$watch('bookmarkdisplay', function (value) {
        if (value) {
            $scope.wbi.displayProjectBookmarkBtn = true;
        }
        else {
            $scope.wbi.displayProjectBookmarkBtn = false;
        }
    });

    $scope.saveBookmark = function (row) {
        var pobj;
        var index = -1;
        Notification.info({message: 'Please wait', title: 'Processing', replaceMessage: true});
        row.bookmark = !row.bookmark;
        index = $scope.rowCollection.indexOf(row);
        $scope.rowCollection[index] = row;
        pobj = $filter('filter')($scope.rowCollection, {rid: row.rid});
        //save into db
        $timeout(function () {
            $http.post('/api/revision/saveBookmark?rid=' + row.rid + "&bookmark=" + row.bookmark)
                .then(function (ret) {
                    $http.get('/api/revision/clearCache?rid=' + row.rid)
                        .then(function () {
                            Notification.then('Bookmark ' + row.displayName + " ... Done");
                            Notification.then({
                                message: 'Bookmark ' + row.displayName + " ... Done",
                                replaceMessage: true
                            });
                        });
                });
        }, 1000);
    };
});



