App.controller('CustomerProgramCtrl', function ($scope, $rootScope, $http, $sce, $stateParams, $filter, $location,
                                                $localStorage, Notification, $mdDialog, $mdSidenav,
                                                infoFactory, contactFactory,
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
    $scope.isCustomerProgram = true;
    $scope.isChipProgram = false;

    $scope.milestones = [];
    $scope.remark = "";
    $scope.remarkTimestamp = "";
    $scope.headlineSelected = -1;
    $scope.showHeadlineEditFeature = true;
    $scope.openSideMenu = function () {
        $mdSidenav('left').toggle()
    };
    $scope.openNewTab = function (url) {
        $window.open(url, '_blank');
    };
    $scope.infopredicate1 = 'order';
    $scope.infopredicate2 = 'key';

    $scope.go = function (id, page) {
        var patharr = $location.path().split("/");
        if (typeof patharr != 'undefined') {
            patharr[4] = id;
            if (page.match(/[0-9a-zA-Z]/i)) {
                patharr[5] = page;
            }
            $location.path(patharr.join('/'));
        }
    };
    $http.get('/api/program/getRevisionList?pid=' + $scope.pid)
        .then(function (result) {
            $scope.revisions = result.data;
            $scope.currentRevision = $filter('filter')($scope.revisions, {rid: $scope.rid})[0];
            $scope.stage = $scope.currentRevision['stage'];
            $scope.basedie = $scope.currentRevision['base'];
            $scope.segment = $scope.currentRevision['segment'];
            $scope.rname = $scope.currentRevision['revision'];
            $scope.headline.content = $scope.currentRevision['headline'];
            $scope.programName = $scope.currentRevision['displayName'];
            $scope.headline.timestamp = $filter('filter')($scope.revisions, {rid: $scope.rid})[0]['hlts'];
            $scope.outlook = $scope.currentRevision['outlook'];
            $scope.outlookTimestamp = "<i>Updated on " + $scope.currentRevision['outlookts'] + "</i>";
        }, function (data, status) {
            Notification.error(data);
        });
    if ($scope.page.match(/milestone/i)) {
        $scope.ms = {};
        $scope.ms.snapshotSelect = '';
        $scope.projectCatName = 'project';
        $scope.ms.flagSelect = {};
        $scope.ms.gid = 0;
        $scope.ms.tid = 0;
        $scope.ms.allowEdit = true;
        $scope.ms.currentCategory = 'project';
        $scope.ms.categories = [];
        $scope.milestones = [];
        $scope.ms.automaticStatusSelected = true;
        $scope.ganttData = [];
        $scope.predicate = 'torder';
        $scope.snapshotMilestone = false;
        $scope.displayMilestoneStrikeout = false;

        $scope.currentMilestoneRowClick = {};
        if ($rootScope.authority.match(/^(admin|pm)/i)) {
            $scope.datePickerOptions = {
                formatYear: 'yy',
                startingDay: 1,
                'show-weeks': false,
            };
            $scope.actualDatePickerOptions = {
                formatYear: 'yy',
                startingDay: 1,
                'show-weeks': false,
            };
        }
        else {
            $scope.minDate = new Date(today.getFullYear(), today.getMonth() - 1, today.getDate());
            $scope.actualMaxDate = new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1);
            $scope.datePickerOptions = {
                formatYear: 'yy',
                startingDay: 1,
                'show-weeks': false,
                minDate: $scope.minDate
            };
            $scope.actualDatePickerOptions = {
                formatYear: 'yy',
                startingDay: 1,
                'show-weeks': false,
                maxDate: $scope.actualMaxDate,
                minDate: $scope.minDate
            };
        }

        $scope.displayMilestoneSnapshot = function (val) {
            $scope.displayMilestoneStrikeout = !$scope.displayMilestoneStrikeout;
        };
        $scope.flagStatus = "black";
        milestoneFactory.getCategory($scope.rid, "", 0)
            .then(function (result) {
                if (result) {
                    $scope.ms.categories = $filter('orderBy')(result.data, ['order', 'name']);
                    var pobj = $filter('filter')($scope.ms.categories, {name: 'project'}, true);
                    $scope.ms.gid = pobj[0]['id'];
                    $scope.ms.currentCategory = pobj[0]['name'];
                }
            }, function (data, code) {
                Notification.error(data);
            });

        $scope.$watch('ms.gid', function (gid) {
            if (gid != 0) {
                var pobj = $filter('filter')($scope.ms.categories, {id: parseInt(gid)}, true);
                $scope.ms.currentCategory = pobj[0]['name'];
                milestoneFactory.getMilestoneSnapshot(gid, '', 0)
                    .then(function (result) {
                        $scope.snapshots = result.data;
                    }, function (data, code) {
                        Notification.error(data);
                        console.log(data);
                    });
                milestoneFactory.getIndicator($scope.rid, gid, '', 0)
                    .then(function (result) {
                        $scope.milestones = $filter('orderBy')(result.data, ['torder', 'tname']);
                    }, function (data, code) {
                        Notification.error(data);
                        console.log(data);
                    });
            }
        });

        $scope.addNewCat = function (event) {
            resetMilestoneSelected();
            //should use modal here
            var confirm = $mdDialog.prompt()
                .title('Add New Category ?')
                .textContent('Note: New Category name should be unique.')
                .placeholder('New category name')
                .ariaLabel('New Category name')
                .initialValue('')
                .targetEvent(event)
                .ok('Save!')
                .cancel('Close');

            $mdDialog.show(confirm).then(function (result) {
                var checkcat = $filter('filter')($scope.ms.categories, {name: result.toLowerCase().trim()}, true);
                if (typeof checkcat != 'undefined' && checkcat.length > 0) {
                    Notification.error('ERR: Category Name Already added');
                }
                else {
                    //save
                    var cat = {};
                    cat.name = result;
                    cat.order = 1;
                    cat.rid = $scope.rid;
                    $http.post("/api/revision/addIndicatorCategory", JSON.stringify(cat))
                        .then(function (new_cat) {
                            milestoneFactory.getCategory($scope.rid, "", 1)
                                .then(function (result) {
                                    if (result) {
                                        $scope.ms.categories = $filter('orderBy')(result.data, ['order', 'name']);
                                        $scope.ms.gid = new_cat.data.gid;
                                        $scope.ms.currentCategory = new_cta.data.name;
                                    }
                                }, function (data, code) {
                                    Notification.error(data);
                                });
                        });
                }
            });

        };

        $scope.$watch('milestoneSelected.currentEnd.optionModel', function (val) {
            if (typeof val != 'undefined') {
                if (!val.match(/date/i)) {
                    $scope.allowOrange = false;
                }
                else {
                    $scope.allowOrange = true;
                }
            }
        });

        $scope.checkNewFormShowCondition = function (milestoneeditform) {
            resetMilestoneSelected();
            $scope.milestoneSelected.isNew = true;
            if (typeof $scope.ms.currentCategory != 'undefined' && !$scope.ms.currentCategory.match(/^project$/i)) {
                milestoneeditform.$show();
            }
            else {
                if (typeof $scope.ms.currentCategory != 'undefined1') {
                    Notification.error("Please select category first");
                }
                else if ($scope.ms.currentCategory.match(/^project$/i)) {
                    Notification.error("Project Category Milestone must be populated from Template");
                }
            }
        };

        $scope.removeCat = function () {
            resetMilestoneSelected();
            var confirm = $mdDialog.confirm()
                .title("Remove Category (Permanent)?")
                .textContent('Are you sure you want to permanently ' + $scope.ms.currentCategory + ' Category?')
                .ok("Delete")
                .cancel("Cancel");
            $mdDialog.show(confirm).then(function () {
                var cat = $filter('filter')($scope.ms.categories, {id: $scope.ms.gid}, true)[0];
                var index = $scope.ms.categories.indexOf(cat);
                $http.post("/api/revision/removeIndicatorCategory", JSON.stringify({
                    'gid': $scope.ms.gid,
                    'rid': $scope.rid
                }))
                    .then(function (ret) {
                        $scope.ms.categories.splice(index, 1);
                        var pobj = $filter('filter')($scope.ms.categories, {name: 'project'}, true);
                        $scope.ms.gid = pobj[0]['id'];
                        $scope.ms.currentCategory = pobj[0]['name'];
                        if (ret.hasOwnProperty('pgid')) {
                            milestoneFactory.getCategory($scope.rid, '', 1)
                                .then(function (result) {
                                    if (result) {
                                        $scope.ms.categories = result;
                                    }
                                }, function (data, code) {
                                    Notification.error(data);
                                });
                        }
                    });
            });
        };

        $scope.refreshMilestone = function () {
            $scope.snapshotMilestone = false;
            milestoneFactory.getCategory($scope.rid, "", 1)
                .then(function (result) {
                    if (result) {
                        $scope.ms.categories = $filter('orderBy')(result.data, ['order', 'name']);
                        var pobj = $filter('filter')($scope.ms.categories, {id: $scope.ms.gid}, true);
                        $scope.ms.currentCategory = pobj[0]['name'];

                    }
                }, function (data, code) {
                    console.log(data);
                });
            milestoneFactory.getIndicator($scope.rid, $scope.ms.gid, '', 1)
                .then(function (result) {
                    $scope.milestones = $filter('orderBy')(result.data, ['torder', 'tname']);
                }, function (data, code) {
                    Notification.error(data);
                    console.log(data);
                });
        };

        $scope.$watch('ms.snapshotSelect', function (val) {
            $scope.snapshotMilestone = false;
            if (val.match(/[0-9]/i)) {
                $scope.snapshotMilestone = true;
                var arr = val.split(/-/);
                val = arr[1] + '/' + arr[2] + '/' + arr[0];
            }
            if ($scope.ms.gid > 0) {
                milestoneFactory.getCategory($scope.rid, val, 0)
                    .then(function (result) {
                        if (result) {
                            $scope.ms.categories = $filter('orderBy')(result.data, ['order', 'name']);
                            var pobj = $filter('filter')($scope.ms.categories, {id: $scope.ms.gid}, true);
                            $scope.ms.currentCategory = pobj[0]['name'];
                        }
                    }, function (data, code) {
                        console.log(data);
                    });
                milestoneFactory.getIndicator($scope.rid, $scope.ms.gid, val, 0)
                    .then(function (result) {
                        $scope.milestones = $filter('orderBy')(result.data, ['torder', 'tname']);
                    }, function (data, code) {
                        Notification.error(data);
                        console.log(data);
                    });
            }
        });

        $scope.$watch('milestoneSelected.currentEnd.date', function (val) {
            $scope.allowOrange = false;
            if (typeof val != 'undefined') {
                var diff = 0;
                var aDate = new Date();
                var today = new Date(aDate.getTime() - aDate.getTime() % 86400000);
                if (val instanceof Date) {
                    diff = val.getTime() - today.getTime();
                }
                else {
                    if (val.match(/^\d{2}\/\d{2}\/\d{2,4}$/)) {
                        var dtparts = val.split(/\//);
                        if (dtparts[2].match(/\d{4}/)) {
                            dt = new Date(dtparts[2], dtparts[0] - 1, dtparts[1]);
                        }
                        else {
                            dt = new Date("20" + dtparts[2], dtparts[0] - 1, dtparts[1]);
                        }
                        diff = dt.getTime() - today.getTime();
                    }
                }
                if (diff < 0) {
                    $scope.allowOrange = false;
                    $scope.milestoneSelected.currentEnd.dhstatus = 'black';
                    $scope.milestoneSelected.tstatus = 'black';
                }
                else {
                    $scope.allowOrange = true;
                }
            }

        });

        $scope.checkCurrentEndColor = function (dt) {
            if ($scope.milestoneSelected.currentEnd.snapshot.match(/^\d{2}\/\d{2}\/\d{2,4}$/)) {
                var parts = $scope.milestoneSelected.currentEnd.snapshot.split(/\//);
                var old_date;
                var aDate = new Date();
                var today = new Date(aDate.getTime() - aDate.getTime() % 86400000);
                if (parts[2].match(/\d{4}/)) {
                    old_date = new Date(parts[2], parts[0] - 1, parts[1]);
                }
                else {
                    old_date = new Date("20" + parts[2], parts[0] - 1, parts[1]);
                }
                if (dt instanceof Date) {
                    var diff = dt.getTime() - old_date.getTime();
                    var diff2 = dt.getTime() - today.getTime();
                    if (diff2 < 0) {
                        $scope.allowOrange = false;
                        $scope.flagStatus = 'grey';
                        $scope.milestoneSelected.currentEnd.dhstatus = 'grey';
                    }
                    else {
                        if (diff > 0) {
                            $scope.allowOrange = false;
                            $scope.flagStatus = 'red';
                            $scope.milestoneSelected.currentEnd.dhstatus = 'red';
                        }
                        else if (diff < 0) {
                            $scope.allowOrange = true;
                            $scope.flagStatus = 'green';
                            $scope.milestoneSelected.currentEnd.dhstatus = 'green';
                        }
                        else {
                            $scope.allowOrange = true;
                            $scope.flagStatus = 'black';
                            $scope.milestoneSelected.currentEnd.dhstatus = 'black';
                        }
                    }
                }
                else {
                    if (dt.match(/^\d{2}\/\d{2}\/\d{2,4}$/)) {
                        var dtparts = dt.split(/\//);
                        if (dtparts[2].match(/\d{4}/)) {
                            dt = new Date(dtparts[2], dtparts[0] - 1, dtparts[1]);
                        }
                        else {
                            dt = new Date("20" + dtparts[2], dtparts[0] - 1, dtparts[1]);
                        }
                        var diff = dt.getTime() - old_date.getTime();
                        var diff2 = dt.getTime() - today.getTime();
                        if (diff2 < 0) {
                            $scope.allowOrange = false;
                            $scope.flagStatus = 'grey';
                            $scope.milestoneSelected.currentEnd.dhstatus = 'grey';
                        }
                        else {
                            if (diff > 0) {
                                $scope.allowOrange = false;
                                $scope.flagStatus = 'red';
                                $scope.milestoneSelected.currentEnd.dhstatus = 'red';
                            }
                            else if (diff < 0) {
                                $scope.allowOrange = true;
                                $scope.flagStatus = 'green';
                                $scope.milestoneSelected.currentEnd.dhstatus = 'green';
                            }
                            else {
                                $scope.allowOrange = true;
                                $scope.flagStatus = 'black';
                                $scope.milestoneSelected.currentEnd.dhstatus = 'black';
                            }
                        }
                    }
                    else {
                        $scope.allowOrange = true;
                        $scope.flagStatus = 'black';
                        $scope.milestoneSelected.currentEnd.dhstatus = 'black';
                    }
                }
            }
        };

        $scope.removeMilestone = function () {
            if (typeof $scope.milestoneSelected.index == 'undefined' || $scope.milestoneSelected.index < 0) {
                Notification.error('Please select milestone that you would like to remove.');
            }
            else {
                var confirm = $mdDialog.confirm()
                    .title("Delete Confirmation (Permanent)")
                    .textContent('Are you sure you want to delete milestone ' + $scope.milestoneSelected.name + '?')
                    .ok("Delete")
                    .cancel("Cancel");
                $mdDialog.show(confirm).then(function () {
                    $http.post('/api/revision/removeIndicatorTask', JSON.stringify($scope.milestoneSelected))
                        .then(function () {
                            milestoneFactory.getIndicator($scope.rid, $scope.ms.gid, '', 1)
                                .then(function (result) {
                                    $scope.milestones = $filter('orderBy')(result.data, ['torder', 'tname']);
                                }, function (data, code) {
                                    Notification.error(data);
                                    console.log(data);
                                });
                        });
                });
            }
        };

        $scope.milestoneRowClick = function (index, $event, milestone) {
            $scope.currentMilestoneRowClick.index = index;
            $scope.currentMilestoneRowClick.milestone = milestone;
            $scope.milestoneclicked = true;
            resetMilestoneSelected();
            $scope.ms.orangeStatus = false;
            $scope.ms.allowEdit = true;
            milestoneFactory.parseRowClick($scope.currentMilestoneRowClick.milestone, function (retObj) {
                $scope.milestoneSelected.planStart.date = retObj.milestonePlanStartDate;
                $scope.milestoneSelected.planStart.comment = retObj.milestonePlanStartComment.replace(/\&nbsp\;/i, '');
                $scope.milestoneSelected.planStart.optionModel = retObj.milestonePlanStartDateOptionModel;

                $scope.milestoneSelected.actualStart.date = retObj.milestoneActualStartDate;
                $scope.milestoneSelected.actualStart.comment = retObj.milestoneActualStartComment.replace(/\&nbsp\;/i, '');
                $scope.milestoneSelected.actualStart.optionModel = retObj.milestoneActualStartDateOptionModel;

                $scope.milestoneSelected.currentEnd.date = retObj.milestonePlanEndDate;
                $scope.milestoneSelected.currentEnd.comment = retObj.milestonePlanEndComment.replace(/\&nbsp\;/i, '');
                $scope.milestoneSelected.currentEnd.optionModel = retObj.milestonePlanEndDateOptionModel;
                $scope.milestoneSelected.currentEnd.snapshot = retObj.milestonePlanEndSnapshot;

                $scope.milestoneSelected.actualEnd.date = retObj.milestoneActualEndDate;
                $scope.milestoneSelected.actualEnd.comment = retObj.milestoneActualEndComment.replace(/\&nbsp\;/i, '');
                $scope.milestoneSelected.actualEnd.optionModel = retObj.milestoneActualEndDateOptionModel;
                $scope.milestoneSelected.actualEnd.snapshot = retObj.milestoneActualEndSnapshot;

                $scope.milestoneSelected.isNew = false;
                $scope.milestoneSelected.tstatus = retObj.milestoneStatus;

                if ($scope.milestoneSelected.tstatus.match(/orange/i)) {
                    $scope.ms.orangeStatus = true;
                }
                else if (!$scope.milestoneSelected.tstatus.match(/[0-9a-zA-Z]/i)) {
                    $scope.ms.allowEdit = false;
                }
            });
            $scope.milestoneSelected.index = $scope.currentMilestoneRowClick.index;
            $scope.milestoneSelected.tid = $scope.currentMilestoneRowClick.milestone.id;
            $scope.milestoneSelected.order = $scope.currentMilestoneRowClick.milestone.torder;
            $scope.milestoneSelected.name = $scope.currentMilestoneRowClick.milestone.tname;
            $scope.milestoneSelected.note = '';
            if ($scope.currentMilestoneRowClick.milestone.tnote) {
                $scope.milestoneSelected.note = $scope.currentMilestoneRowClick.milestone.tnote.replace(/<(?:.|\n)*?>/gm, '').replace(/\[\d{2}\/\d{2}\/\d{2,4}\], ''/).replace(/\&nbsp\;/i, '');
            }
        };

        $scope.checkEditFormShowCondition = function (milestoneeditform) {
            var msg = milestoneFactory.checkEditFormShow(milestoneeditform, $scope.milestoneSelected);
            if (msg.match(/[a-zA-Z]/)) {
                Notification.error(msg);
            }
        };

        $scope.saveMilestone = function () {
            $scope.milestoneSelected.tstatus = $scope.flagStatus;
            if (typeof $scope.milestoneSelected.planStart != 'undefined' && typeof $scope.milestoneSelected.planStart.optionModel != 'undefined') {
                if ($scope.milestoneSelected.planStart.optionModel.match(/na/i)) {
                    $scope.milestoneSelected.planStart.value = 'NA';
                    $scope.milestoneSelected.planStart.dhstatus = 'grey';
                }
                else if ($scope.milestoneSelected.planStart.optionModel.match(/tbd/i)) {
                    $scope.milestoneSelected.planStart.value = 'TBD';
                    $scope.milestoneSelected.planStart.dhstatus = 'grey';
                }
                else if ($scope.milestoneSelected.planStart.optionModel.match(/blank/i)) {
                    $scope.milestoneSelected.planStart.value = '';
                    $scope.milestoneSelected.planStart.dhstatus = 'black';
                }
                else if ($scope.milestoneSelected.planStart.optionModel.match(/date/i)) {
                    $scope.milestoneSelected.planStart.value = $rootScope.convertDateToString($scope.milestoneSelected.planStart.date);
                    $scope.milestoneSelected.planStart.dhstatus = 'black';
                }
            }
            if (typeof $scope.milestoneSelected.actualStart != 'undefined' && typeof $scope.milestoneSelected.actualStart.optionModel != 'undefined') {
                if ($scope.milestoneSelected.actualStart.optionModel.match(/na/i)) {
                    $scope.milestoneSelected.actualStart.value = 'NA';
                    $scope.milestoneSelected.actualStart.dhstatus = 'grey';
                }
                else if ($scope.milestoneSelected.actualStart.optionModel.match(/tbd/i)) {
                    $scope.milestoneSelected.actualStart.value = 'TBD';
                    $scope.milestoneSelected.actualStart.dhstatus = 'grey';
                }
                else if ($scope.milestoneSelected.actualStart.optionModel.match(/blank/i)) {
                    $scope.milestoneSelected.actualStart.value = '';
                    $scope.milestoneSelected.actualStart.dhstatus = 'black';
                }
                else if ($scope.milestoneSelected.actualStart.optionModel.match(/date/i)) {
                    $scope.milestoneSelected.actualStart.value = $rootScope.convertDateToString($scope.milestoneSelected.actualStart.date);
                    $scope.milestoneSelected.actualStart.dhstatus = 'black';
                }
            }
            if (typeof $scope.milestoneSelected.actualEnd != 'undefined' && typeof $scope.milestoneSelected.actualEnd.optionModel != 'undefined') {
                if ($scope.milestoneSelected.actualEnd.optionModel.match(/na/i)) {
                    $scope.milestoneSelected.actualEnd.value = 'NA';
                    $scope.milestoneSelected.actualEnd.dhstatus = 'grey';
                }
                else if ($scope.milestoneSelected.actualEnd.optionModel.match(/tbd/i)) {
                    $scope.milestoneSelected.actualEnd.value = 'TBD';
                    $scope.milestoneSelected.actualEnd.dhstatus = 'grey';
                }
                else if ($scope.milestoneSelected.actualEnd.optionModel.match(/blank/i)) {
                    $scope.milestoneSelected.actualEnd.value = '';
                    $scope.milestoneSelected.actualEnd.dhstatus = 'black';
                }
                else if ($scope.milestoneSelected.actualEnd.optionModel.match(/date/i)) {
                    $scope.milestoneSelected.actualEnd.value = $rootScope.convertDateToString($scope.milestoneSelected.actualEnd.date);
                    $scope.milestoneSelected.actualEnd.dhstatus = 'black';
                }
            }
            if (typeof $scope.milestoneSelected.currentEnd != 'undefined' && typeof $scope.milestoneSelected.currentEnd.optionModel != 'undefined') {
                if ($scope.milestoneSelected.currentEnd.optionModel.match(/na/i)) {
                    $scope.milestoneSelected.currentEnd.value = 'NA';
                    $scope.milestoneSelected.currentEnd.dhstatus = 'grey';
                }
                else if ($scope.milestoneSelected.currentEnd.optionModel.match(/tbd/i)) {
                    $scope.milestoneSelected.currentEnd.value = 'TBD';
                    $scope.milestoneSelected.currentEnd.dhstatus = 'grey';
                }
                else if ($scope.milestoneSelected.currentEnd.optionModel.match(/blank/i)) {
                    $scope.milestoneSelected.currentEnd.value = '';
                    $scope.milestoneSelected.currentEnd.dhstatus = 'black';
                }
                else if ($scope.milestoneSelected.currentEnd.optionModel.match(/date/i)) {
                    $scope.milestoneSelected.currentEnd.value = $rootScope.convertDateToString($scope.milestoneSelected.currentEnd.date);
                    $scope.milestoneSelected.currentEnd.dhstatus = 'black';
                    $scope.milestoneSelected.tstatus = 'black';
                    if ($scope.ms.orangeStatus) {
                        if ($scope.milestoneSelected.currentEnd.dhstatus.match(/(green|black)/i)) {
                            $scope.milestoneSelected.currentEnd.dhstatus = 'orange';
                            $scope.milestoneSelected.tstatus = 'orange';
                        }
                    }
                    else {
                        if ($scope.milestoneSelected.currentEnd.snapshot.match(/[0-9]/)) {
                            var d1 = $rootScope.convertStringToDate($scope.milestoneSelected.currentEnd.date);
                            var d2 = $rootScope.convertStringToDate($scope.milestoneSelected.currentEnd.snapshot);
                            var diff = d1.getTime() - d2.getTime();
                            var diff2 = new Date().getTime() - d1.getTime();
                            if (new Date().getTime() > d1.getTime()) {
                                $scope.milestoneSelected.currentEnd.dhstatus = 'grey';
                                $scope.milestoneSelected.tstatus = 'black';
                            }
                            else {
                                if (diff > 0) {
                                    $scope.milestoneSelected.currentEnd.dhstatus = 'red';
                                    $scope.milestoneSelected.tstatus = 'red';
                                }
                                else if (diff < 0) {
                                    $scope.milestoneSelected.currentEnd.dhstatus = 'green';
                                    $scope.milestoneSelected.tstatus = 'green';
                                }
                                else {
                                    $scope.milestoneSelected.currentEnd.dhstatus = 'black';
                                    $scope.milestoneSelected.tstatus = 'black';
                                }
                            }
                        }
                    }
                }
            }
            milestoneFactory.getCategoryStatus($scope.milestones, $scope.milestoneSelected.tid, function (gstatus) {
                $scope.milestoneSelected.gstatus = gstatus;
                if (!$scope.milestoneSelected.gstatus.match(/red/i)) {
                    if ($scope.milestoneSelected.tstatus.match(/red/i)) {
                        $scope.milestoneSelected.gstatus = "red";
                    }
                    else {
                        if (!$scope.milestoneSelected.gstatus.match(/orange/i)) {
                            if ($scope.milestoneSelected.tstatus.match(/orange/i)) {
                                $scope.milestoneSelected.gstatus = "orange";
                            }
                            else {
                                if ($scope.milestoneSelected.tstatus.match(/green/i)) {
                                    $scope.milestoneSelected.gstatus = "green";
                                }
                                else {
                                    $scope.milestoneSelected.gstatus = "black";
                                }
                            }
                        }
                    }
                }
            });
            $scope.milestoneSelected.gid = $scope.ms.gid;
            $scope.milestoneSelected.rid = $scope.rid;
            setTimeout(function () {
                $http.post('/api/revision/saveIndicatorTask', JSON.stringify($scope.milestoneSelected))
                    .then(function (ret) {
                        var current_category = $filter('filter')($scope.ms.categories, {id: $scope.ms.gid}, true)[0];

                        current_category.status = ret.data.gstatus;
                        if (current_category.name.match(/project/i)) {
                            // var revobj = $filter('filter')($scope.revisions, {rid: $scope.rid})[0];
                            $scope.currentRevision.revision_btn_color = ret.data.revision_btn_color;
                        }
                        console.log("Hello World");
                        milestoneFactory.getCategory($scope.rid, "", 1)
                            .then(function (result) {
                                if (result) {
                                    $scope.ms.categories = $filter('orderBy')(result.data, ['order', 'name']);
                                    var pobj = $filter('filter')($scope.ms.categories, {id: $scope.ms.gid}, true);
                                    $scope.ms.currentCategory = pobj[0]['name'];

                                }
                            }, function (data, code) {
                                console.log(data);
                            });
                        milestoneFactory.getIndicator($scope.rid, $scope.ms.gid, '', 1)
                            .then(function (result) {
                                $scope.milestones = $filter('orderBy')(result.data, ['torder', 'tname']);

                            }, function (data, code) {
                                Notification.error(data);
                                console.log(data);
                            });
                    }, function (data, code) {
                        Notification.error(data);
                    });
            }, 10);

        };

    }
    else if ($scope.page.match(/weeklyupdate/i)) {
        $scope.remark = {};
        $scope.remark.snapshotSelect = '';
        $scope.remark.content = '';
        $scope.remark.tinymce = '';
        $scope.remark.ts = '';
        $scope.remark.gid = 0;
        $scope.remark.currentCategory = 'project';
        $scope.remark.categories = [];
        $scope.snapshotRemark = false;
        $scope.remark.tinymce = '';
        $scope.remark.isContentChanged = false;
        milestoneFactory.getCategory($scope.rid, "", 0)
            .then(function (result) {
                if (result) {
                    $scope.remark.categories = $filter('orderBy')(result.data, ['order', 'name']);
                    var pobj = $filter('filter')($scope.remark.categories, {name: 'PROJECT'});
                    $scope.remark.gid = pobj[0]['id'];
                    $scope.remark.content = $sce.trustAsHtml(pobj[0]['remark']);
                    $scope.remark.tinymce = $scope.remark.content;
                    $scope.remark.ts = "<i>Updated on " + pobj[0]['ts'] + "</i>";
                    $scope.remark.isContentChanged = false;
                }
            }, function (data, code) {
                console.log(data);
            });
        $scope.$watch("remark.gid", function (gid) {
            $scope.snapshotRemark = false;
            $scope.remark.snapshotSelect = '';
            if (gid > 0) {
                var pobj = $filter('filter')($scope.remark.categories, {id: gid});
                if (pobj) {
                    $scope.remark.gid = pobj[0]['id'];
                    $scope.remark.content = $sce.trustAsHtml(pobj[0]['remark']);
                    $scope.remark.tinymce = $scope.remark.content;
                    $scope.remark.ts = "<i>Updated on " + pobj[0]['ts'] + "</i>";
                    $scope.remark.isContentChanged = false;
                }
                else {
                    remarkFactory.get(gid, '', 0)
                        .then(function (result) {
                            if (result) {
                                $scope.remark.content = $sce.trustAsHtml(result.data.remark);
                                $scope.remark.ts = "<i>Updated on " + result.data.ts + "</i>";
                                $scope.remark.tinymce = result['remark'];
                                $scope.remark.isContentChanged = false;
                            }
                        }, function (data, status) {
                            Notification.error(data);
                        });
                }
                milestoneFactory.getRemarkSnapshot(gid)
                    .then(function (result) {
                        $scope.snapshots = result.data;
                    }, function (data, code) {
                        console.log(data);
                    });
            }
        });
        $scope.$watch('remark.snapshotSelect', function (val) {
            $scope.snapshotRemark = false;
            if (val.match(/[0-9]/i)) {
                $scope.snapshotRemark = true;
                var arr = val.split(/-/);
                val = arr[1] + '/' + arr[2] + '/' + arr[0];
            }
            if ($scope.remark.gid > 0) {
                milestoneFactory.getCategory($scope.rid, val, 0)
                    .then(function (result) {
                        if (result) {
                            $scope.remark.categories = $filter('orderBy')(result.data, ['order', 'name']);
                            var pobj = $filter('filter')($scope.remark.categories, {id: $scope.remark.gid});
                            $scope.remark.content = $sce.trustAsHtml(pobj[0]['remark']);
                            $scope.remark.tinymce = $scope.remark.content;
                            $scope.remark.ts = "<i>Updated on " + pobj[0]['ts'] + "</i>";
                            $scope.remark.isContentChanged = false;
                        }
                    }, function (data, code) {
                        console.log(data);
                    });
            }
        });
        $scope.clickCategory = function (cat) {
            $scope.remark.gid = cat.id;
            $scope.remark.currentCategory = cat.name;
        };
    }
    else if ($scope.page.match(/info$/i)) {
        $scope.informationTable = {};
        infoFactory.getInfoTable($scope.rid, 0)
            .then(function (result) {
                $scope.informationTable = result.data;
            });
    }
    else if ($scope.page.match(/dashboard$/i)) {
        infoFactory.getInfoDashboard($scope.rid, 0, function (result) {
            $scope.informationDashboard = result.data;
        });
        contactFactory.get($scope.rid, 0)
            .then(function (result) {
                if (Array.isArray(result.data)) {
                    $scope.contacts = result.data;
                }
            }, function (data, status) {
                console.log(data);
            });
        headlineFactory.getSnapshot($scope.rid)
            .then(function (ret) {
                $scope.headline.snapshots = ret.data;
            });

        $scope.$watch('headline.snapshotSelect', function (val) {
            if (val.match(/[0-9]/)) {
                $scope.snapshotHeadline = true;
                var arr = val.split(/-/);
                val = arr[1] + '/' + arr[2] + '/' + arr[0];
            }
            else {
                $scope.snapshotHeadline = false;
                val = "";
            }
            headlineFactory.get($scope.rid, val, 0)
                .then(function (result) {
                    if (Array.isArray(result.data.headline)) {
                        if (result.data.headline.length == 1 && result.data.headline[0] == "") {
                            $scope.headline.content = "";
                            $scope.headline.timestamp = result.data['hlts'];
                            $scope.resource_flag = result.data['resource_flag'];
                            $scope.budget_flag = result.data['budget_flag'];
                            $scope.project_flag = result.data['prediction_flag'];
                        }
                        else {
                            $scope.headline.content = result.data['headline'];
                            $scope.headline.timestamp = result.data['hlts'];
                            $scope.resource_flag = result.data['resource_flag'];
                            $scope.budget_flag = result.data['budget_flag'];
                            $scope.project_flag = result.data['prediction_flag'];
                        }
                    }
                }, function (data, status) {
                    $scope.emptyHeadline = false;
                    Notification.error(data);
                });
        });
        linkFactory.get($scope.rid, 0, false)
            .then(function (result) {
                if (result.data['meeting']) {
                    $scope.meetings = result.data['meeting'];
                }
                if (result.data['link']) {
                    $scope.links = result.data['link'];
                }
            }, function (data, code) {
                Notification.error(data);
            });
        milestoneFactory.getFrontPage($scope.rid, 0)
            .then(function (result) {
                $scope.milestones = $filter('orderBy')(result.data, 'order');
            });


    }
    else if ($scope.page.match(/settings/i)) {
        setTimeout(function () {
            $scope.settings = {};
            if (typeof $scope.currentRevision == 'undefined') {
                $scope.currentRevision = $filter('filter')($scope.revisions, {rid: $scope.rid})[0];
            }
            $scope.settings.program = $scope.currentRevision.program;
            $scope.settings.revision = $scope.currentRevision.revision;
            $scope.settings.base = $scope.currentRevision.base;
            $scope.settings.stage = $scope.currentRevision.stage;
            $scope.settings.scheduleFlag = $filter('filter')($scope.flaglist, {flag: $scope.currentRevision.schedule_flag}, true)[0];
            $scope.settings.escalationFlag = $filter('filter')($scope.flaglist, {flag: $scope.currentRevision.prediction_flag}, true)[0];
            $scope.settings.status = true;
            if (typeof $scope.currentRevision.status != 'undefined') {
                $scope.settings.status = ($scope.currentRevision.status.toLowerCase() === 'true');
            }
            $scope.$watch('settings.scheduleFlag', function (val) {
                if (val.flag.match(/grey/i)) {
                    $scope.settings.status = false;
                }
                else {
                    $scope.settings.status = true;
                }
            });
            $scope.$watch('settings.status', function (val) {
                if (!val) {
                    $scope.settings.scheduleFlag = $filter('filter')($scope.flaglist, {flag: 'grey'}, true)[0];
                    $scope.settings.escalationFlag = $filter('filter')($scope.flaglist, {flag: 'grey'}, true)[0];
                }
            });

            $scope.saveSettings = function () {
                $http.post("/api/revision/saveRevisionFlag", {
                    'rid': $scope.rid,
                    'schedule': $scope.settings.scheduleFlag.flag,
                    'base': $scope.settings.base,
                    'stage': $scope.settings.stage,
                    'escalation': $scope.settings.escalationFlag.flag,
                    'program': $scope.settings.program,
                    'revision': $scope.settings.revision,
                    'status': $scope.settings.status
                })
                    .then(function (result) {
                        $scope.currentRevision.schedule_flag = $scope.settings.scheduleFlag.flag;
                        $scope.currentRevision.revision_btn_color = $scope.settings.scheduleFlag.flag;
                        if ($scope.currentRevision.revision_btn_color.match(/black/i)) {
                            $scope.currentRevision.revision_btn_color = 'green';
                        }
                        $scope.currentRevision.prediction_flag = $scope.settings.escalationFlag.flag;
                        $scope.currentRevision.escalation_btn_color = $scope.settings.escalationFlag.flag;
                        if ($scope.currentRevision.escalation_btn_color.match(/black/i)) {
                            $scope.currentRevision.escalation_btn_color = 'green';
                        }
                        $scope.currentRevision.status = $scope.settings.status;
                        $scope.currentRevision.program = $scope.settings.program;
                        $scope.currentRevision.base = $scope.settings.base;
                        $scope.currentRevision.revision = $scope.settings.revision;
                        $scope.rname = $scope.settings.revision;
                        $scope.programName = $scope.settings.program;
                    }, function (data, code) {
                        Notification.error(data);
                    });
            };
        }, 100);

    }


    /***********************************************************
     * Project Contact action handle
     **********************************************************/

    $scope.filterContact = function (contact) {
        return contactFactory.filter(contact);
    };

    $scope.cancelSaveContact = function (infos) {
        $scope.contacts = contactFactory.cancel($scope.contacts);
    };
    $scope.addNewContact = function (obj) {
        obj = contactFactory.add(obj);
    };

    $scope.saveContactTable = function () {
        contactFactory.save($scope.rid, $scope.contacts, function () {
            contactFactory.get($scope.rid, 1)
                .then(function (result) {
                    if (Array.isArray(result.data)) {
                        $scope.contacts = result.data;
                    }
                }, function (data, code) {
                    Notification.error(data);
                });

        });
    };
    $scope.deleteContact = function (contactArr) {
        $scope.contacts = contactFactory.del($scope.contacts, contactArr.id);
    };
    $scope.refreshContact = function (level) {
        contactFactory.get($scope.rid, 1)
            .then(function (result) {
                if (Array.isArray(result.data)) {
                    $scope.contacts = result.data;
                }
            }, function (data, code) {
                Notification.error(data);
            });
    };

    /***********************************************************
     * Project Information action handle
     **********************************************************/
    $scope.filterInfoTable = function (info) {
        return infoFactory.filter(info);
    };


    $scope.addNewInfoDashboard = function (obj) {
        obj = infoFactory.foDasboard(obj);
    };

    $scope.cancelSaveInfoDashboard = function () {
        $scope.informationDashboard = infoFactory.cancelInfoDashboard($scope.informationDashboard);
    };

    $scope.filterInfoTable = function (info) {
        return infoFactory.filter(info);
    };

    $scope.cancelSaveInfoTable = function (infos) {
        $scope.informationTable.body = infoFactory.cancel($scope.informationTable.data);
    };
    $scope.addNewInfo = function (obj) {
        obj = infoFactory.addInfoTable(obj);
    };

    $scope.saveInfoDashboard = function () {
        var obj = {};
        obj.type = 'dashboard';
        obj.rid = $scope.rid;
        obj.data = $scope.informationDashboard.data;
        if ('category' in $scope.informationDashboard) {
            obj.data.push($scope.informationDashboard.category);
        }
        if ('technology' in $scope.informationDashboard) {
            obj.data.push($scope.informationDashboard.technology);
        }
        if ('type' in $scope.informationDashboard) {
            obj.data.push($scope.informationDashboard.type);
        }
        if ('dft' in $scope.informationDashboard) {
            obj.data.push($scope.informationDashboard.dft);
        }
        setTimeout(function () {
            infoFactory.saveInfoTable($scope.rid, obj, function () {
                infoFactory.getInfoDashboard($scope.rid, 1, function (result) {
                    $scope.informationDashboard = result.data;
                });
            });
        }, 10);
    };

    $scope.saveInfoRevChange = function () {
        var obj = {};
        obj.type = 'dashboard';
        obj.rid = $scope.rid;
        obj.data = $scope.informationDashboard.revChange;
        setTimeout(function () {
            infoFactory.saveInfoTable($scope.rid, obj, function () {
                infoFactory.getInfoDashboard($scope.rid, 1, function (result) {
                    $scope.informationDashboard.revChange = result.data.revChange;
                });
            });
        }, 10);
    };

    $scope.saveInfoTable = function () {
        $scope.infoloaded = false;
        var obj = {};
        obj.type = 'detail';
        obj.data = $scope.informationTable.data;
        infoFactory.saveInfoTable($scope.rid, obj, function () {
            infoFactory.getInfoTable($scope.rid, 1)
                .then(function (result) {
                    $scope.informationTable = result.data;
                });
        });
    };

    $scope.deleteInfoTable = function (info) {
        $scope.informationTable = infoFactory.del($scope.informationTable, info);
    };
    $scope.refreshInfoTable = function () {
        infoFactory.getInfoTable($scope.rid, 1)
            .then(function (result) {
                $scope.informationTable = result.data;
            });
    };

    $scope.refreshInfoDashboard = function () {
        $scope.informationDashboard = [];
        infoFactory.getInfoDashboard($scope.rid, 1, function (result) {
            $scope.informationDashboard = result.data;
        });
    };

    $scope.refreshInfoRevChange = function () {
        infoFactory.getInfoDashboard($scope.rid, 1, function (result) {
            $scope.informationDashboard.revChange = result.data.revChange;
        });
    };
    /***********************************************************
     * Project Link and Meeting action handle
     **********************************************************/

    $scope.refreshLink = function () {
        linkFactory.get($scope.rid, 1, false)
            .then(function (result) {
                if (result.data['link']) {
                    $scope.links = result.data['link'];
                }
                if (result.data['meeting']) {
                    $scope.meetings = result.data['meeting'];
                }
            }, function (data, code) {
                Notification.error(data);
            });
    };
    $scope.filterLink = function (meeting) {
        return linkFactory.filter(meeting);
    };
    $scope.addNewLink = function (obj) {
        obj = linkFactory.add(obj);
    };
    $scope.deleteLink = function (id) {
        $scope.links = linkFactory.del($scope.links, id);
    };
    $scope.cancelSaveLink = function () {
        $scope.links = linkFactory.cancel($scope.links);
    };
    $scope.saveLinkTable = function () {
        linkFactory.save($scope.rid, $scope.links, "link", function () {
            linkFactory.get($scope.rid, 1, false)
                .then(function (result) {
                    if (result.data['link']) {
                        $scope.links = result.data['link'];
                    }
                }, function (data, code) {
                    Notification.error(data);
                });
        })
    };

    $scope.deleteMeeting = function (id) {
        $scope.meetings = linkFactory.del($scope.meetings, id);
    };
    $scope.cancelSaveMeeting = function () {
        $scope.meetings = linkFactory.cancel($scope.meetings);
    };
    $scope.saveMeetingTable = function () {
        linkFactory.save($scope.rid, $scope.meetings, "meeting", function () {
            linkFactory.get($scope.rid, 1, false)
                .then(function (result) {
                    if (result.data['meeting']) {
                        $scope.meetings = result.data['meeting'];
                    }
                })
                .error(function (data, code) {
                    Notification.error(data);
                });
        })
    };

    /***********************************************************
     * Outlook action handle
     **********************************************************/
    $scope.refreshOutlook = function () {
        outlookFactory.get($scope.rid, 1)
            .then(function (result) {
                if (result.data['outlook']) {
                    $scope.outlook = result.data['outlook'];
                    $scope.outlookTimestamp = "<i>Updated on " + result.data['ts'] + "</i>";
                }
            }, function (data, status) {
                Notification.error(data);
            });
    };

    $scope.saveOutlook = function (outlook) {
        outlookFactory.save($scope.rid, outlook)
            .then(function (result) {
                outlookFactory.get($scope.rid, 1)
                    .then(function (result) {
                        if (result.data['outlook']) {
                            $scope.outlook = result.data['outlook'];
                            $scope.outlookTimestamp = "<i>Updated on " + result.data['ts'] + "</i>";
                        }
                    }, function (data, status) {
                        Notification.error(data);
                    });
            }, function (ret, status) {
                Notification.error({message: ret, delay: 5000, title: status});
            })
    };

    /***********************************************************
     * Remark action handle
     **********************************************************/
    $scope.refreshRemark = function () {
        remarkFactory.get($scope.remark.gid, '', 1)
            .then(function (result) {
                $scope.remarkloaded = true;
                if (result.data['remark']) {
                    $scope.remark.content = $sce.trustAsHtml(result.data['remark']);
                    $scope.remark.ts = "<i>Updated on " + result.data['ts'] + "</i>";
                    $scope.remark.tinymce = result.data['remark'];
                    $scope.remark.isContentChanged = false;
                }
            }, function (data, status) {
                Notification.error(data);
            });
    };

    $scope.saveRemark = function () {
        $http.post('/api/revision/saveRemark', JSON.stringify({
            'remark': $scope.remark.tinymce,
            'gid': $scope.remark.gid
        }))
            .then(function (ret) {
                remarkFactory.get($scope.remark.gid, '', 1)
                    .then(function (result) {
                        if (result.data['remark']) {
                            $scope.remark.content = $sce.trustAsHtml(result.data['remark']);
                            $scope.remark.ts = "<i>Updated on " + result.data['ts'] + "</i>";
                            $scope.remark.tinymce = result.data['remark'];
                            $scope.remark.isContentChanged = false;
                        }
                    }, function (data, status) {
                        Notification.error(data);
                    });

                milestoneFactory.getRemarkSnapshot($scope.remark.gid)
                    .then(function (result) {
                        $scope.snapshots = result.data;
                    }, function (data, code) {
                        console.log(data);
                    });
            });
    };

    /***********************************************************
     * Headline action handle
     **********************************************************/
    $scope.refreshHeadline = function () {
        headlineFactory.get($scope.rid, '', 1)
            .then(function (result) {
                if (Array.isArray(result.data.headline)) {
                    if (result.data.headline.length == 1 && result.data.headline[0] == "") {
                        $scope.headline.content = result.data['headline'];
                        $scope.headline.timestamp = result.data['hlts'];
                        $scope.resource_flag = result.data['resource_flag'];
                        $scope.budget_flag = result.data['budget_flag'];
                        $scope.project_flag = result.data['prediction_flag'];
                    }
                }
            }, function (data, status) {
                $scope.emptyHeadline = false;
                $scope.headlineloaded = true;
                Notification.error(data);
            });
    };
    $scope.flag = [{class: 'btn-success'}, {class: 'btn-warning'}, {class: 'btn-error'}];

    $scope.$watch('headlineSelected', function (val) {
        if (typeof val != 'undefined') {
            if (val == -1) {
                $scope.showHeadlineEditFeature = false;
            }
            else {
                $scope.showHeadlineEditFeature = true;
            }
        }
    });
    $scope.$watchCollection("headlines", function (newVal, oldVal) {
        if (typeof newVal != 'undefined' && newVal.length > 0) {
            if (newVal[0].match(/[0-9a-zA-Z]+/)) {
                if ($scope.headlineSelected == -1) {
                    $scope.showHeadlineEditFeature = false;
                }
                else {
                    $scope.showHeadlineEditFeature = true;
                }
            }
            else {
                $scope.showHeadlineEditFeature = false;
            }
        }
        else {
            $scope.showHeadlineEditFeature = false;
        }
    });
    $scope.headlineSection = {};
    $scope.currentHeadlineClick = {};
    //get index of headline click
    $scope.headlineClick = function (index, e, hl) {
        $scope.showEditFeature = true;
        $scope.currentHeadlineClick.index = index;
        $scope.currentHeadlineClick.content = hl;
    };

    $scope.parseHeadline = function () {
        if (typeof $scope.currentHeadlineClick.index != 'undefined') {
            $scope.headlineSelected = $scope.currentHeadlineClick.index;
            $scope.headlineSection.status = "";
            $scope.headlineSection.issue = "";
            $scope.headlineSection.nextStep1 = "";
            $scope.headlineSection.dept1 = "";
            $scope.headlineSection.owner1 = "";
            $scope.headlineSection.nextStep2 = "";
            $scope.headlineSection.dept2 = "";
            $scope.headlineSection.owner2 = "";
            $scope.headlineSection.nextStep3 = "";
            $scope.headlineSection.dept3 = "";
            $scope.headlineSection.owner3 = "";
            var arr = headlineFactory.parse($scope.currentHeadlineClick.content);
            if ("headlineStatus" in arr) {
                $scope.headlineSection.status = arr.headlineStatus;
            }
            if ("saveHeadlineType" in arr) {
                $scope.saveHeadlineType = arr.saveHeadlineType;
            }
            if ("headlineIssue" in arr) {
                $scope.headlineSection.issue = arr.headlineIssue;
            }
            if ("headlineNextStep1" in arr) {
                $scope.headlineSection.nextStep1 = arr.headlineNextStep1;
            }
            if ("headlineNextStep2" in arr) {
                $scope.headlineSection.nextStep2 = arr.headlineNextStep2;
            }
            if ("headlineNextStep3" in arr) {
                $scope.headlineSection.nextStep3 = arr.headlineNextStep3;
            }
            if ("headlineDept1" in arr) {
                $scope.headlineSection.dept1 = arr.headlineDept1;
            }
            if ("headlineDept2" in arr) {
                $scope.headlineSection.dept2 = arr.headlineDept2;
            }
            if ("headlineDept3" in arr) {
                $scope.headlineSection.dept3 = arr.headlineDept3;
            }
            if ("headlineOwner1" in arr) {
                $scope.headlineSection.owner1 = arr.headlineOwner1;
            }
            if ("headlineOwner2" in arr) {
                $scope.headlineSection.owner2 = arr.headlineOwner2;
            }
            if ("headlineOwner3" in arr) {
                $scope.headlineSection.owner3 = arr.headlineOwner3;
            }
        }
    };
    // $scope.headlineClick = function(index, e, hl) {
    //     $scope.showEditFeature = true;

    // }
    $scope.newHeadline = function () {
        $scope.headlineSection.status = "";
        $scope.headlineSection.issue = "";
        $scope.headlineSection.nextStep1 = "";
        $scope.headlineSection.dept1 = "";
        $scope.headlineSection.owner1 = "";
        $scope.headlineSection.nextStep2 = "";
        $scope.headlineSection.dept2 = "";
        $scope.headlineSection.owner2 = "";
        $scope.headlineSection.nextStep3 = "";
        $scope.headlineSection.dept3 = "";
        $scope.headlineSection.owner3 = "";

        $scope.headlineSelected = -1;

        $scope.saveHeadlineType = "new";
    };

    $scope.removeHeadline = function () {
        if ($scope.headlineSelected != -1) {
            var confirm = $mdDialog.confirm()
                .title("Remove Selected Headline Section")
                .textContent("Are you sure you want to delete selected section? ")
                .ok("Delete")
                .cancel("Cancel");
            $mdDialog.show(confirm).then(function () {
                headlineFactory.del($scope.headline.content, $scope.headlineSelected);
                headlineFactory.save($scope.rid, $scope.headline.content, "internal")
                    .then(function (result) {
                        headlineFactory.get($scope.rid, '', 1)
                            .then(function (result) {
                                $scope.headline.content = result.data['headline'];
                                $scope.headline.timestamp = result.data['hlts'];
                                $scope.resource_flag = result.data['resource_flag'];
                                $scope.budget_flag = result.data['budget_flag'];
                                $scope.project_flag = result.data['prediction_flag'];
                            }, function (data, status) {
                                $scope.emptyHeadline = false;
                                Notification.error(data);
                            });

                    }, function (data, status) {
                        Notification.error(data);
                    })
            })
        }
        else {
            Notification.error("Please select headline to remove");
        }
    };

    $scope.saveHeadline = function () {
        var content = '';
        var issue = '';
        var isValidHeadline = false;
        var foundIssue = false;
        var foundStatus = false;
        var foundNextStep = false;
        if ($scope.headlineSection.issue != undefined && $scope.headlineSection.issue.match(/[0-9a-zA-Z]/i)) {
            issue += "<p><strong>Issue: </strong>" + $scope.headlineSection.issue + "</p>";
            foundIssue = true;
        }
        if ($scope.headlineSection.status != undefined && $scope.headlineSection.status.match(/[0-9a-zA-Z]/i)) {
            issue += "<p><strong>Status: </strong>" + $scope.headlineSection.status + "</p>";
            foundStatus = true;
        }
        if (($scope.headlineSection.nextStep1 != undefined && $scope.headlineSection.nextStep1.match(/[0-9a-zA-Z]/i))
            || ($scope.headlineSection.nextStep2 != undefined && $scope.headlineSection.nextStep2.match(/[0-9a-zA-Z]/i))
            || ($scope.headlineSection.nextStep3 != undefined && $scope.headlineSection.nextStep3.match(/[0-9a-zA-Z]/i))) {
            foundNextStep = true;
            issue += "<p><strong>Next Steps: </strong></p><ul>";
            if ($scope.headlineSection.nextStep1 != undefined && $scope.headlineSection.nextStep1.match(/[0-9a-zA-Z]/i)) {
                issue += "<li>" + $scope.headlineSection.nextStep1 + " [" + $scope.headlineSection.dept1 + " / " + $scope.headlineSection.owner1 + "]</li>";
            }
            if ($scope.headlineSection.nextStep2 != undefined && $scope.headlineSection.nextStep2.match(/[0-9a-zA-Z]/i)) {
                issue += "<li>" + $scope.headlineSection.nextStep2 + " [" + $scope.headlineSection.dept2 + " / " + $scope.headlineSection.owner2 + "]</li>";
            }
            if ($scope.headlineSection.nextStep3 != undefined && $scope.headlineSection.nextStep3.match(/[0-9a-zA-Z]/i)) {
                issue += "<li>" + $scope.headlineSection.nextStep3 + " [" + $scope.headlineSection.dept3 + " / " + $scope.headlineSection.owner3 + "]</li>";
            }
            issue += "</ul>";
        }
        if (foundIssue) {
            isValidHeadline = false;
            if (foundStatus && foundNextStep) {
                isValidHeadline = true;
            }
            else {
                if (!foundStatus) {
                    this.headlineeditform.$setError("headlineSection.status", "Status can not be empty when there is an issue");
                }
                if (!foundNextStep) {
                    this.headlineeditform.$setError("headlineSection.nextStep1", "Next Step can not be empty when there is an issue");
                }
            }
        }
        else {
            if (foundStatus && !foundNextStep) {
                isValidHeadline = true;
            }
            else {
                if (foundNextStep) {
                    this.headlineeditform.$setError("headlineSection.issue", "Issue can not be empty when there is a Next Step.");
                }
            }
        }
        if (!isValidHeadline) {
            Notification.error({
                title: 'Incorrect entry',
                delay: 10000,
                closeOnClick: 'true',
                message: 'Please enter either a Status alone, or an Issue + Status + Next Steps.',
                positionY: 'top',
                positionX: 'right'
            });
            return "Incorrect entry. Please enter either a Status alone, or an Issue + Status + Next Steps";
        }
        if ($scope.saveHeadlineType == "edit") {
            $scope.headline.content[$scope.headlineSelected] = issue;
        } else if ($scope.saveHeadlineType == "new") {
            $scope.headline.content.unshift(issue);
        }
        setTimeout(function () {
            headlineFactory.save($scope.rid, $scope.headline.content, "chip")
                .then(function (result) {
                    headlineFactory.get($scope.rid, '', 1)
                        .then(function (result) {
                            $scope.headline.content = result.data['headline'];
                            $scope.headline.timestamp = result.data['hlts'];
                            $scope.resource_flag = result.data['resource_flag'];
                            $scope.budget_flag = result.data['budget_flag'];
                            $scope.project_flag = result.data['prediction_flag'];
                        }, function (data, status) {
                            $scope.emptyHeadline = false;
                            Notification.error(data);
                        });
                }, function (data, status) {
                    Notification.error(data);
                });
        }, 10);
    };

    function resetMilestoneSelected() {
        $scope.milestoneSelected = {};
        $scope.milestoneSelected.index = -1;
        $scope.milestoneSelected.tid = 0;
        $scope.milestoneSelected.order = 0;
        $scope.milestoneSelected.name = '';
        $scope.milestoneSelected.note = '';
        $scope.milestoneSelected.tstatus = 'black';
        $scope.milestoneSelected.gstatus = 'black';

        $scope.milestoneSelected.planStart = {};
        $scope.milestoneSelected.planStart.date = '';
        $scope.milestoneSelected.planStart.value = '';
        $scope.milestoneSelected.planStart.comment = '';
        $scope.milestoneSelected.planStart.dhstatus = 'black';
        $scope.milestoneSelected.planStart.optionModel = '';

        $scope.milestoneSelected.actualStart = {};
        $scope.milestoneSelected.actualStart.date = '';
        $scope.milestoneSelected.actualStart.value = '';
        $scope.milestoneSelected.actualStart.comment = '';
        $scope.milestoneSelected.actualStart.dhstatus = 'black';
        $scope.milestoneSelected.actualStart.optionModel = '';

        $scope.milestoneSelected.currentEnd = {};
        $scope.milestoneSelected.currentEnd.date = '';
        $scope.milestoneSelected.currentEnd.value = '';
        $scope.milestoneSelected.currentEnd.comment = '';
        $scope.milestoneSelected.currentEnd.dhstatus = 'black';
        $scope.milestoneSelected.currentEnd.optionModel = '';
        $scope.milestoneSelected.currentEnd.snapshot = '';

        $scope.milestoneSelected.actualEnd = {};
        $scope.milestoneSelected.actualEnd.date = '';
        $scope.milestoneSelected.actualEnd.value = '';
        $scope.milestoneSelected.actualEnd.comment = '';
        $scope.milestoneSelected.actualEnd.dhstatus = 'black';
        $scope.milestoneSelected.actualEnd.optionModel = '';
        $scope.milestoneSelected.actualEnd.snapshot = '';

        $scope.milestoneSelected.isNew = false;

    }

    $scope.refreshFrontPageMilestone = function () {
        milestoneFactory.getFrontPage($scope.rid, 1)
            .then(function (result) {
                $scope.milestones = $filter('orderBy')(result.data, 'order');
            });
    };

});