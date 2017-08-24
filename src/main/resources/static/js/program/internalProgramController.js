App.controller('InternalProgramCtrl', function ($scope, $rootScope, $http, $sce, $stateParams, $filter, $location,
                                        $localStorage, Notification, $mdDialog,
                                        Upload, infoFactory, contactFactory,
                                        linkFactory, skuFactory, headlineFactory,
                                        swFactory,
                                        outlookFactory, milestoneFactory, remarkFactory,
                                        resourceFactory, ipChipTableFactory, authenticationFactory) {
    $scope.pid = $stateParams.pid;
    $scope.rid = $stateParams.rid;
    $scope.revisions=[];
    $scope.page = $stateParams.page;
    $scope.stage='';
    $scope.trustAsHtml = $sce.trustAsHtml;

    $scope.headline={};
    $scope.headline.snapshots=[];
    $scope.snapshotHeadline = false;
    $scope.headline.snapshotSelect = '';
    $scope.infoDashboard = [];
    $scope.currentRevision = {};
    $scope.rname = '';
    $scope.programName = '';
    $scope.alerts=[];

    $scope.meetings = [];
    $scope.links = [];
    $scope.outlook = "";
    $scope.outlookTimestamp = "";
    $scope.resources = [];
    $scope.resourceChart = [];
    $scope.resourceTimeArray = [];

    $scope.milestones = [];
    $scope.remark = "";
    $scope.remarkTimestamp = "";
    $scope.headlineSelected = -1;
    $scope.showHeadlineEditFeature = true;
    $scope.openNewTab = function(url){
        $window.open(url, '_blank');
    };

    $scope.go = function (id, page) {
        var patharr = $location.path().split("/");
        if (typeof patharr != 'undefined'){
            patharr[4] = id;
            if(page.match(/[0-9a-zA-Z]/i)){
                patharr[5] = page;
            }
            $location.path(patharr.join('/'));
        }
    };

    var today = new Date();
    $scope.minDate = new Date(today.getFullYear(), today.getMonth()-1, today.getDate());
    $scope.actualMaxDate = new Date(today.getFullYear(), today.getMonth(), today.getDate()+1);
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

    $scope.clearRevisionCache= function(){
        $http.post('/revision/clearRevisionCache', {'rid': $scope.rid}).then(function(result){
            Notification.success({message: 'Revision Cache cleared, Ready to be refreshed', delay:2000,replaceMessage:true, positionY:'top', positionX:'center'});
            $timeout(function(){
                location.reload();
            }, 1000);
        });
    };
    $scope.clearProgramCache= function(){
        $http.post('/revision/clearRevisionCache', {'rid': $scope.rid, 'pid':$scope.pid}).then(function(result){
            Notification.success({message: 'Cache cleared. Ready to be refreshed', delay:2000,replaceMessage:true, positionY:'top', positionX:'center'});
            $timeout(function(){
                location.reload();
            }, 1000);
        });
    };

    $scope.settingDashboard = {};

    //get all revision list
    $http.get('/api/program/getRevisionList?pid='+$scope.pid)
        .then(function(result){
            $scope.revisions = result.data;
            $scope.currentRevision = $filter('filter')($scope.revisions, {rid : $scope.rid	})[0];
            $scope.stage = $scope.currentRevision['stage'];
            $scope.basedie = $scope.currentRevision['base'];
            $scope.segment = $scope.currentRevision['segment'];
            $scope.rname = $scope.currentRevision['revision'];
            $scope.headline.content = $scope.currentRevision['headline'];
            $scope.programName = $scope.currentRevision['displayName'];
            $scope.headline.timestamp = $filter('filter')($scope.revisions, {rid : $scope.rid	})[0]['hlts'];
            $scope.outlook = $scope.currentRevision['outlook'];
            $scope.outlookTimestamp = "<i>Updated on "+ $scope.currentRevision['outlookts'] + "</i>";
        },function(data, status){
            Notification.error(data);
        });

    if($scope.page.match(/milestone/i)){
        $scope.ms={};
        $scope.ms.snapshotSelect = '';
        $scope.projectCatName = 'project';
        $scope.ms.flagSelect={};
        $scope.ms.gid = 0;
        $scope.ms.tid = 0;
        $scope.ms.allowEdit = true;
        $scope.ms.currentCategory = 'project';
        $scope.ms.categories = [];
        $scope.milestones=[];
        $scope.ms.automaticStatusSelected = true;
        $scope.ganttData = [];
        $scope.predicate='torder';
        $scope.snapshotMilestone = false;
        $scope.displayMilestoneStrikeout = false;
        if($rootScope.authority.match(/^(admin|pm)/i)){
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
        else{
            $scope.minDate = new Date(today.getFullYear(), today.getMonth()-1, today.getDate());
            $scope.actualMaxDate = new Date(today.getFullYear(), today.getMonth(), today.getDate()+1);
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

        $scope.displayMilestoneSnapshot = function(val){
            $scope.displayMilestoneStrikeout = !$scope.displayMilestoneStrikeout;
        }
        resetMilestoneSelected();
        $scope.flagStatus =  "black";
        milestoneFactory.getCategory($scope.rid, "", 0)
            .then(function(result){
                if(result){
                    $scope.ms.categories = result.data;
                    var pobj = $filter('filter')($scope.ms.categories, {name:'project'}, true);
                    $scope.ms.gid = pobj[0]['id'];
                    $scope.ms.currentCategory = pobj[0]['name'];
                }
            }, function(data, code){
                Notification.error(data);
            });
        $scope.$watch('ms.gid', function(gid){
            if(gid != 0){
                resetMilestoneSelected();
                var pobj = $filter('filter')($scope.ms.categories, {id: parseInt(gid)}, true);
                $scope.ms.currentCategory = pobj[0]['name'];
                milestoneFactory.getMilestoneSnapshot(gid, '', 0)
                    .then(function(result){
                        $scope.snapshots = result.data;
                    }, function(data, code){
                        Notification.error(data);
                        console.log(data);
                    });
                milestoneFactory.getIndicator($scope.rid, gid, '', 0)
                    .then(function(result){
                        $scope.milestones = result.data;
                    }, function(data, code){
                        Notification.error(data);
                        console.log(data);
                    });
            }
        });

        $scope.refreshMilestone = function(){
            $scope.snapshotMilestone = false;
            milestoneFactory.getCategory($scope.rid, "", 1)
                .then(function(result){
                    if(result){
                        $scope.ms.categories = $filter('orderBy')(result.data, ['order', 'name']);
                        var pobj = $filter('filter')($scope.ms.categories, {id:$scope.ms.gid}, true);
                        $scope.ms.currentCategory = pobj[0]['name'];

                    }
                }, function(data, code){
                    console.log(data);
                });
            milestoneFactory.getIndicator($scope.rid, $scope.ms.gid, '', 1)
                .then(function(result){
                    $scope.milestones = result.data;
                }, function(data, code){
                    Notification.error(data);
                    console.log(data);
                });
        };

        $scope.$watch('ms.snapshotSelect', function(val){
            $scope.snapshotMilestone = false;
            resetMilestoneSelected();
            if(val.match(/[0-9]/i)){
                $scope.snapshotMilestone = true;
                var arr= val.split(/-/);
                val = arr[1]+'/'+arr[2]+'/'+arr[0];
            }
            if($scope.ms.gid>0){
                milestoneFactory.getCategory($scope.rid, val, 0)
                    .then(function(result){
                        if(result){
                            $scope.ms.categories = $filter('orderBy')(result.data, ['order', 'name']);
                            var pobj = $filter('filter')($scope.ms.categories, {id:$scope.ms.gid}, true);
                            $scope.ms.currentCategory = pobj[0]['name'];
                        }
                    }, function(data, code){
                        console.log(data);
                    });
                milestoneFactory.getIndicator($scope.rid, $scope.ms.gid, val, 0)
                    .then(function(result){
                        $scope.milestones = result.data;
                    }, function(data, code){
                        Notification.error(data);
                        console.log(data);
                    });
            }
        });


    }
    else if ($scope.page.match(/info$/i)){
        $scope.informationTable = {};
        infoFactory.getInfoTable($scope.rid, 0)
            .then(function(result){
                $scope.informationTable = result.data;
            });
    }
    else if($scope.page.match(/sku$/i)){
        $scope.skus = [];
        skuFactory.get($scope.pid, 0)
            .then(function(result) {
                if (result.data) {
                    $scope.skus = result.data;
                }
            }, function(data, code) {
                Notification.error(data);
            });
    }
    else if($scope.page.match(/dashboard$/i)){
        infoFactory.getInfoDashboard($scope.rid, 0, function(result){
            $scope.informationDashboard = result.data;
        });
        contactFactory.get($scope.rid, 0)
            .then(function(result){
                if(Array.isArray(result.data)){
                    $scope.contacts = result.data;
                }
            }, function (data, status){
                console.log(data);
            });
        headlineFactory.getSnapshot($scope.rid)
            .then(function(ret){
                $scope.headline.snapshots=ret.data;
            });

        $scope.$watch('headline.snapshotSelect', function(val){
            if(val.match(/[0-9]/)){
                $scope.snapshotHeadline = true;
                var arr = val.split(/-/);
                val = arr[1]+'/'+arr[2]+'/'+arr[0];
            }
            else{
                $scope.snapshotHeadline = false;
                val = "";
            }
            headlineFactory.get($scope.rid, val, 0)
                .then(function(result) {
                    if(Array.isArray(result.data.headline)){
                        if(result.data.headline.length==1 && result.data.headline[0]  == ""){
                            $scope.headline.content = "";
                            $scope.headline.timestamp = result.data['hlts'];
                            $scope.resource_flag = result.data['resource_flag'];
                            $scope.budget_flag = result.data['budget_flag'];
                            $scope.project_flag = result.data['prediction_flag'];
                        }
                        else{
                            $scope.headline.content = result.data['headline'];
                            $scope.headline.timestamp = result.data['hlts'];
                            $scope.resource_flag = result.data['resource_flag'];
                            $scope.budget_flag = result.data['budget_flag'];
                            $scope.project_flag = result.data['prediction_flag'];
                        }
                    }
                },function(data, status) {
                $scope.emptyHeadline = false;
                Notification.error(data);
            });
        });
        linkFactory.get($scope.rid, 0, false)
            .then(function(result) {
                if (result.data['meeting']) {
                    $scope.meetings = result.data['meeting'];
                }
                if (result.data['link']) {
                    $scope.links = result.data['link'];
                }
            }, function(data, code) {
                Notification.error(data);
            });
        milestoneFactory.getFrontPage($scope.rid, 0)
            .then(function(result) {
                $scope.milestones = result.data;
            });


        ipChipTableFactory.get($scope.rid, 0)
            .then(function(result){
                if(Array.isArray(result.data)){
                    $scope.ipChipTable = result.data;
                }
            });
        // var chart = {};
        // $scope.resourceMonthlyChartConfig = {
        //     credits : {
        //         enabled : false
        //     },
        //     options : {
        //         chart:{},
        //         plotOptions : {
        //             line : {
        //                 states : {
        //                     hover : {
        //                         lineWidth : 3
        //                     }
        //                 },
        //             },
        //             series : {
        //                 marker : {
        //                     enabled : true
        //                 }
        //             },
        //             showInLegend : true
        //         }
        //     },func: function(chart) {
        //         $timeout(function() {
        //             chart.reflow();
        //         }, 10);
        //     },
        //     title : {},
        //     subtitle : {},
        //     xAxis : {
        //         labels : {
        //             rotation : -60,
        //             style : {
        //                 fontSize : '12px',
        //                 fontFamily : 'Verdana, sans-serif'
        //             },
        //             overflow : 'justify'
        //         },
        //         min : 0
        //     },
        //     yAxis : {
        //         title : {
        //             text : 'Headcount'
        //         },
        //         min : 0,
        //         allowDecimals : false
        //     },
        //     legend : {
        //         enabled : true,
        //         align : 'center',
        //     },
        //     tooltip : {
        //         crosshairs : true,
        //         shared : true
        //     },
        //     exporting : {},
        // };
        //
        // $scope.subtitleChart = '';
        // $scope.milestoneResourceValid = false;
        //
        // resourceFactory.getMonthlyChart($scope.rid,0)
        //     .then(function(result) {
        //         if(!result.data){
        //             $scope.milestoneResourceValid = false;
        //         }
        //         else{
        //             $scope.milestoneResourceValid = true;
        //             $scope.resourceMonthlyChartConfig.options.chart.type = 'line';
        //             $scope.resourceMonthlyChartConfig.series=  result.data.series;
        //             $scope.resourceMonthlyChartConfig.xAxis.categories= result.data.time;
        //             $scope.resourceMonthlyChartConfig.title.text=  result.data.title;
        //             $scope.resourceMonthlyChartConfig.exporting.fileName =$scope.subtitleChart;
        //         }
        //     }, function(data, code) {
        //         console.log(data);
        //     });
        // resourceFactory.getSummaryTable($scope.rid,0)
        //     .then(function(result) {
        //         if(result){
        //             $scope.milestoneResourceValid = true;
        //             $scope.resources = result.data['data'];
        //             $scope.resourceTimestamp = result.data['last_updated_date'];
        //         }
        //         else{
        //             $scope.milestoneResourceValid = false;
        //             $scope.resourceTimestamp = "<i>No Data Available</i>";
        //         }
        //     }, function(data, code) {
        //         Notification.error(data);
        //     });

    }














    /***********************************************************
     * Project Contact action handle
     **********************************************************/

    $scope.filterContact = function(contact) {
        return contactFactory.filter(contact);
    };

    $scope.cancelSaveContact = function(infos) {
        $scope.contacts = contactFactory.cancel($scope.contacts);
    };
    $scope.addNewContact = function(obj) {
        obj = contactFactory.add(obj);
    };

    $scope.saveContactTable = function() {
        contactFactory.save($scope.rid, $scope.contacts, function(){
            contactFactory.get($scope.rid, 1)
                .then(function(result) {
                    if(Array.isArray(result.data)){
                        $scope.contacts = result.data;
                    }
                }, function(data, code) {
                    Notification.error(data);
                });

        });
    };
    $scope.deleteContact = function(contactArr) {
        $scope.contacts = contactFactory.del($scope.contacts, contactArr.id);
    }
    $scope.refreshContact = function(level) {
        contactFactory.get($scope.rid, 1)
            .then(function (result) {
                if (Array.isArray(result.data)) {
                    $scope.contacts = result.data;
                }
            }, function (data, code) {
                Notification.error(data);
            });
    }

    /***********************************************************
     * Project Information action handle
     **********************************************************/

    $scope.addNewInfoDashboard = function(obj){
        obj = infoFactory.foDasboard(obj);
    };

    $scope.cancelSaveInfoDashboard = function(){
        $scope.informationDashboard = infoFactory.cancelInfoDashboard($scope.informationDashboard);
    };

    $scope.filterInfoTable = function(info) {
        return infoFactory.filter(info);
    };

    $scope.cancelSaveInfoTable = function(infos) {
        $scope.informationTable.body = infoFactory.cancel($scope.informationTable.data);
    };
    $scope.addNewInfo = function(obj) {
        obj = infoFactory.addInfoTable(obj);
    };

    $scope.saveInfoDashboard = function(){
        var obj = {};
        obj.type = 'dashboard';
        obj.rid = $scope.rid;
        obj.data = $scope.informationDashboard.data;
        if('category' in $scope.informationDashboard){
            obj.data.push($scope.informationDashboard.category);
        }
        if('technology' in $scope.informationDashboard){
            obj.data.push($scope.informationDashboard.technology);
        }
        if('type' in $scope.informationDashboard){
            obj.data.push($scope.informationDashboard.type);
        }
        if('dft' in $scope.informationDashboard){
            obj.data.push($scope.informationDashboard.dft);
        }
        $timeout(function(){
            infoFactory.saveInfoTable($scope.rid, obj, function(){
                infoFactory.getInfoDashboard($scope.rid, 1, function(result){
                    $scope.informationDashboard = result.data;
                });
            });
        }, 10);
    }

    $scope.saveInfoRevChange = function(){
        var obj = {};
        obj.type = 'dashboard';
        obj.rid = $scope.rid;
        obj.data = $scope.informationDashboard.revChange;
        $timeout(function(){
            infoFactory.saveInfoTable($scope.rid, obj, function(){
                infoFactory.getInfoDashboard($scope.rid, 1, function(result){
                    $scope.informationDashboard.revChange = result.data.revChange;
                });
            });
        }, 10);
    }

    $scope.saveInfoTable = function() {
        $scope.infoloaded = false;
        var obj = {};
        obj.type = 'detail';
        obj.data = $scope.informationTable.data;
        infoFactory.saveInfoTable($scope.rid, obj, function(){
            infoFactory.getInfoTable($scope.rid, 1)
                .then(function(result){
                    $scope.informationTable = result.data;
                });
        });
    };

    $scope.deleteInfoTable = function(info) {
        $scope.informationTable = infoFactory.del($scope.informationTable, info);
    }
    $scope.refreshInfoTable = function() {
        infoFactory.getInfoTable($scope.rid, 1)
            .then(function(result){
                $scope.informationTable = result.data;
            });
    }

    $scope.refreshInfoDashboard = function() {
        $scope.informationDashboard = [];
        infoFactory.getInfoDashboard($scope.rid, 1, function(result){
            $scope.informationDashboard = result.data;
        });
    }

    $scope.refreshInfoRevChange= function(){
        infoFactory.getInfoDashboard($scope.rid, 1, function(result){
            $scope.informationDashboard.revChange = result.data.revChange;
        });
    }
    /***********************************************************
     * Project Link and Meeting action handle
     **********************************************************/

    $scope.refreshLink = function() {
        linkFactory.get($scope.rid, 1, false)
            .then(function(result) {
                if (result.data['link']) {
                    $scope.links = result.data['link'];
                }
                if (result.data['meeting']) {
                    $scope.meetings = result.data['meeting'];
                }
            }, function(data, code) {
                Notification.error(data);
            });
    }
    $scope.filterLink = function(meeting) {
        return linkFactory.filter(meeting);
    };
    $scope.addNewLink = function(obj) {
        obj = linkFactory.add(obj);
    };
    $scope.deleteLink = function(id) {
        $scope.links = linkFactory.del($scope.links, id);
    }
    $scope.cancelSaveLink = function() {
        $scope.links = linkFactory.cancel($scope.links);
    };
    $scope.saveLinkTable = function() {
        linkFactory.save($scope.rid, $scope.links, "link", function(){
            linkFactory.get($scope.rid, 1, false)
                .then(function(result) {
                    if (result.data['link']) {
                        $scope.links = result.data['link'];
                    }
                }, function(data, code) {
                    Notification.error(data);
                });
        })
    };

    $scope.deleteMeeting = function(id) {
        $scope.meetings = linkFactory.del($scope.meetings, id);
    }
    $scope.cancelSaveMeeting = function() {
        $scope.meetings = linkFactory.cancel($scope.meetings);
    };
    $scope.saveMeetingTable = function() {
        linkFactory.save($scope.rid, $scope.meetings, "meeting", function(){
            linkFactory.get($scope.rid, 1, false)
                .then(function(result) {
                    if (result.data['meeting']) {
                        $scope.meetings = result.data['meeting'];
                    }
                })
                .error(function(data, code) {
                    Notification.error(data);
                });
        })
    };

    /***********************************************************
     * Outlook action handle
     **********************************************************/
    $scope.refreshOutlook = function() {
        outlookFactory.get($scope.rid, 1)
            .then( function(result) {
                if (result.data['outlook']) {
                    $scope.outlook = result.data['outlook'];
                    $scope.outlookTimestamp = "<i>Updated on "+ result.data['ts'] + "</i>";
                }
            }, function(data, status) {
                Notification.error(data);
            });
    };

    $scope.saveOutlook = function(outlook) {
        outlookFactory.save($scope.rid, outlook)
            .then(function(result) {
                outlookFactory.get($scope.rid, 1)
                    .then( function(result) {
                        if (result.data['outlook']) {
                            $scope.outlook = result.data['outlook'];
                            $scope.outlookTimestamp = "<i>Updated on "+ result.data['ts'] + "</i>";
                        }
                    }, function(data, status) {
                        Notification.error(data);
                    });
            }, function(ret, status) {
                Notification.error({message:ret, delay: 5000, title:status});
            })
    };

    /***********************************************************
     * Sku action handle
     **********************************************************/
    $scope.refreshSku = function() {
        skuFactory.get($scope.pid, 1)
            .then(function(result) {
                $scope.skus = result.data;
            }, function(data, code) {
                Notification.error(data);
            });
    }
    $scope.filterSku = function(sku) {
        return skuFactory.filter(sku);
    };
    $scope.addNewSku = function(obj) {
        obj = skuFactory.add(obj);
    };
    $scope.deleteSku = function(id) {
        $scope.skus = skuFactory.del($scope.skus, id);
    }
    $scope.cancelSaveSku = function() {
        $scope.skus = skuFactory.cancel($scope.skus);
    };
    $scope.saveSkuTable = function() {
        $scope.skuloaded = false;
        skuFactory.save($scope.pid, $scope.skus, function(){
            skuFactory.get($scope.pid, 1)
                .then(function(result) {
                    $scope.skus = result.data;
                }, function(data, code) {
                    Notification.error(data);
                });
        });
    };

    /***********************************************************
     * Project IP Table action handle
     **********************************************************/

    $scope.filterIPChipTable = function(ip) {
        return ipChipTableFactory.filter(ip);
    };

    $scope.refreshIPChipTable = function(){
        ipChipTableFactory.get($scope.rid, 1)
            .then(function(result){
                if(Array.isArray(result.data)){
                    $scope.ipChipTable = result.data;
                }
            });
    }

    $scope.deleteIPChipTable = function(ip) {
        $scope.ipChipTable = ipChipTableFactory.del($scope.ipChipTable, ip.id);
    };

    $scope.cancelSaveIPChipTable = function() {
        $scope.ipChipTable = ipChipTableFactory.cancel($scope.ipChipTable);
    };
    $scope.addNewIPChipTable = function(obj) {
        obj = ipChipTableFactory.add(obj);
    };

    $scope.saveIPChipTable = function() {
        ipChipTableFactory.save($scope.rid, $scope.ipChipTable, function(){
            ipChipTableFactory.get($scope.rid, 1)
                .then(function(result) {

                    if(Array.isArray(result.data)){
                        $scope.ipChipTable = result.data;
                    }
                })
                .error(function(data, code) {
                    Notification.error(data);
                });

        });
    };

    $scope.searchIPProgram = function(term){
        searchProgramDisplay = [];
        return $http.get('/search/revision',{
            params:{
                'term': term.toLowerCase().replace(/\+/i, 'plus'),
                'type': 'ip'
            }
        }).then(function(res){
            searchProgramDisplay = [];
            searchProgramDisplay = res.data;
            return searchProgramDisplay;
        });
    };

    $scope.selectIP = function($item, $model, ip){
        ip.displayName = $item.pname + " "+$item.rname;
        ip.program = $item.pname;
        ip.revision = $item.rname;
        ip.rev = $item.url;
        ip.pid = $item.pid;
        ip.rid = $item.rid;
    }



    /***********************************************************
     * Remark action handle
     **********************************************************/
    $scope.refreshRemark = function() {
        remarkFactory.get($scope.remark.gid, '', 1)
            .then( function(result) {
                $scope.remarkloaded = true;
                if (result.data['remark']) {
                    $scope.remark.content =  $sce.trustAsHtml(result.data['remark']);
                    $scope.remark.ts = "<i>Updated on "+ result.data['ts'] + "</i>";
                    $scope.remark.tinymce = result.data['remark'];
                    $scope.remark.isContentChanged = false;
                }
            })
            .error(function(data, status) {
                Notification.error(data);
            });
    };

    $scope.saveRemark = function() {
        $http.post('/revision/saveRemark', {'remark':JSON.stringify($scope.remark.tinymce),  'gid':$scope.remark.gid})
            .then(function(ret){
                remarkFactory.get($scope.remark.gid, '', 1)
                    .then( function(result) {
                        if (result.data['remark']) {
                            $scope.remark.content =  $sce.trustAsHtml(result.data['remark']);
                            $scope.remark.ts = "<i>Updated on "+ result.data['ts'] + "</i>";
                            $scope.remark.tinymce = result.data['remark'];
                            $scope.remark.isContentChanged = false;
                        }
                    }, function(data, status) {
                        Notification.error(data);
                    });

                milestoneFactory.getRemarkSnapshot($scope.remark.gid)
                    .then(function(result){
                        $scope.snapshots = result.data;
                    }, function(data, code){
                        console.log(data);
                    });
            });
    };

    /***********************************************************
     * Headline action handle
     **********************************************************/
    $scope.refreshHeadline = function() {
        headlineFactory.get($scope.rid, '', 1)
            .then(function(result) {
                if(Array.isArray(result.data.headline)) {
                    if (result.data.headline.length == 1 && result.data.headline[0] == "") {
                        $scope.headline.content = result.data['headline'];
                        $scope.headline.timestamp = result.data['hlts'];
                        $scope.resource_flag = result.data['resource_flag'];
                        $scope.budget_flag = result.data['budget_flag'];
                        $scope.project_flag = result.data['prediction_flag'];
                    }
                }
            }, function(data, status) {
            $scope.emptyHeadline = false;
            $scope.headlineloaded = true;
            Notification.error(data);
        });
    };
    $scope.flag = [{class:'btn-success'}, {class:'btn-warning'}, {class:'btn-error'}];

    $scope.$watch('headlineSelected', function(val){
        if (typeof val != 'undefined'){
            if(val == -1){
                $scope.showHeadlineEditFeature = false;
            }
            else{
                $scope.showHeadlineEditFeature = true;
            }
        }
    });
    $scope.$watchCollection("headlines", function(newVal, oldVal){
        if(typeof newVal!= 'undefined' && newVal.length >0){
            if(newVal[0].match(/[0-9a-zA-Z]+/)){
                if($scope.headlineSelected == -1){
                    $scope.showHeadlineEditFeature = false;
                }
                else{
                    $scope.showHeadlineEditFeature = true;
                }
            }
            else{
                $scope.showHeadlineEditFeature = false;
            }
        }
        else{
            $scope.showHeadlineEditFeature = false;
        }
    });
    $scope.headlineSection = {};
    $scope.headlineClick = function(index, e, hl) {
        $scope.showEditFeature = true;
        $scope.headlineSelected = index;
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
        var arr = headlineFactory.click(index, e,hl);
        if("headlineStatus" in arr){
            $scope.headlineSection.status = arr.headlineStatus;
        }
        if("saveHeadlineType" in arr){
            $scope.saveHeadlineType = arr.saveHeadlineType;
        }
        if("headlineIssue" in arr){
            $scope.headlineSection.issue = arr.headlineIssue;
        }
        if("headlineNextStep1" in arr){
            $scope.headlineSection.nextStep1 = arr.headlineNextStep1;
        }
        if("headlineNextStep2" in arr){
            $scope.headlineSection.nextStep2 = arr.headlineNextStep2;
        }
        if("headlineNextStep3" in arr){
            $scope.headlineSection.nextStep3 = arr.headlineNextStep3;
        }
        if("headlineDept1" in arr){
            $scope.headlineSection.dept1= arr.headlineDept1;
        }
        if("headlineDept2" in arr){
            $scope.headlineSection.dept2= arr.headlineDept2;
        }
        if("headlineDept3" in arr){
            $scope.headlineSection.dept3= arr.headlineDept3;
        }
        if("headlineOwner1" in arr){
            $scope.headlineSection.owner1 = arr.headlineOwner1;
        }
        if("headlineOwner2" in arr){
            $scope.headlineSection.owner2 = arr.headlineOwner2;
        }
        if("headlineOwner3" in arr){
            $scope.headlineSection.owner3 = arr.headlineOwner3;
        }
    }
    $scope.newHeadline = function() {
        $scope.headlineSection = {};
        $scope.headlineSelected = -1;
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
        $scope.saveHeadlineType = "new";
    };

    $scope.removeHeadline = function() {
        if($scope.headlineSelected != -1){
           var confirm =  $mdDialog.confirm()
                .title("Remove Selected Headline Section")
                .textContent("Are you sure you want to delete selected section? ")
                .ok("Delete")
                .cancel("Cancel");
           $mdDialog.show(confirm).then( function(){
               headlineFactory.del($scope.headline.content, $scope.headlineSelected);
               headlineFactory.save($scope.rid, $scope.headline.content, "internal")
                   .then(function(result) {
                       headlineFactory.get($scope.rid, '', 1)
                           .then(function(result) {
                               $scope.headline.content = result.data['headline'];
                               $scope.headline.timestamp = result.data['hlts'];
                               $scope.resource_flag = result.data['resource_flag'];
                               $scope.budget_flag = result.data['budget_flag'];
                               $scope.project_flag = result.data['prediction_flag'];
                           }, function(data, status) {
                           $scope.emptyHeadline = false;
                           Notification.error(data);
                       });

                   }, function(data,status) {
                       Notification.error(data);
                   })
           })
        }
        else{
            Notification.alert("Please select headline to remove");
        }
    };

    $scope.saveHeadline = function() {
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
            ||($scope.headlineSection.nextStep2!= undefined && $scope.headlineSection.nextStep2.match(/[0-9a-zA-Z]/i))
            || ($scope.headlineSection.nextStep3 != undefined && $scope.headlineSection.nextStep3.match(/[0-9a-zA-Z]/i))) {
            foundNextStep = true;
            issue += "<p><strong>Next Steps: </strong></p><ul>";
            if ($scope.headlineSection.nextStep1 != undefined && $scope.headlineSection.nextStep1.match(/[0-9a-zA-Z]/i)) {
                issue += "<li>" + $scope.headlineSection.nextStep1 + " [" + $scope.headlineSection.dept1 + " / " + $scope.headlineSection.owner1 + "]</li>";
            }
            if ($scope.headlineSection.nextStep2!= undefined && $scope.headlineSection.nextStep2.match(/[0-9a-zA-Z]/i)) {
                issue += "<li>" + $scope.headlineSection.nextStep2 + " [" + $scope.headlineSection.dept2 + " / " + $scope.headlineSection.owner2 + "]</li>";
            }
            if ($scope.headlineSection.nextStep3 != undefined && $scope.headlineSection.nextStep3.match(/[0-9a-zA-Z]/i)) {
                issue += "<li>" + $scope.headlineSection.nextStep3 + " [" + $scope.headlineSection.dept3 + " / " + $scope.headlineSection.owner3 + "]</li>";
            }
            issue += "</ul>";
        }
        if(foundIssue){
            isValidHeadline = false;
            if(foundStatus && foundNextStep){
                isValidHeadline = true;
            }
            else{
                if(!foundStatus){
                    this.headlineeditform.$setError("headlineSection.status", "Status can not be empty when there is an issue");
                }
                if(!foundNextStep){
                    this.headlineeditform.$setError("headlineSection.nextStep1", "Next Step can not be empty when there is an issue");
                }
            }
        }
        else{
            if(foundStatus && !foundNextStep){
                isValidHeadline = true;
            }
            else{
                if(foundNextStep){
                    this.headlineeditform.$setError("headlineSection.issue", "Issue can not be empty when there is a Next Step.");
                }
            }
        }
        if(!isValidHeadline){
            Notification.error({title:'Incorrect entry', delay: 10000, closeOnClick:'true',
                message:'Please enter either a Status alone, or an Issue + Status + Next Steps.', positionY: 'top', positionX: 'right'
            });
            return "Incorrect entry. Please enter either a Status alone, or an Issue + Status + Next Steps";
        }
        if ($scope.saveHeadlineType == "edit") {
            $scope.headline.content[$scope.headlineSelected] = issue;
        } else if ($scope.saveHeadlineType == "new") {
            $scope.headline.content.unshift(issue);
        }
        $timeout(function(){
            headlineFactory.save($scope.rid, $scope.headline.content, $scope.category)
                .then(function(result) {
                    headlineFactory.get($scope.rid, '', 1)
                        .then(function(result) {
                            $scope.headline.content = result.data['headline'];
                            $scope.headline.timestamp = result.data['hlts'];
                            $scope.resource_flag = result.data['resource_flag'];
                            $scope.budget_flag = result.data['budget_flag'];
                            $scope.project_flag = result.data['prediction_flag'];
                        }, function(data, status) {
                        $scope.emptyHeadline = false;
                        Notification.error(data);
                    });
                })
                .error(function(data, status) {
                    Notification.error(data);
                });
        }, 10);
    };

    function resetMilestoneSelected(){
        $scope.milestoneSelected = {};
        $scope.milestoneSelected.index = -1;
        $scope.milestoneSelected.tid = 0;
        $scope.milestoneSelected.order = 0;
        $scope.milestoneSelected.name = '';
        $scope.milestoneSelected.note = '';
        $scope.milestoneSelected.tstatus = 'black';
        $scope.milestoneSelected.gstatus = 'black';

        $scope.milestoneSelected.planStart = {};
        $scope.milestoneSelected.planStart.date='';
        $scope.milestoneSelected.planStart.comment='';
        $scope.milestoneSelected.planStart.dhstatus='tblack';
        $scope.milestoneSelected.planStart.optionModel = '';

        $scope.milestoneSelected.actualStart={};
        $scope.milestoneSelected.actualStart.date = '';
        $scope.milestoneSelected.actualStart.comment = '';
        $scope.milestoneSelected.actualStart.dhstatus = 'tblack';
        $scope.milestoneSelected.actualStart.optionModel = '';

        $scope.milestoneSelected.currentEnd = {};
        $scope.milestoneSelected.currentEnd.date = '';
        $scope.milestoneSelected.currentEnd.comment = '';
        $scope.milestoneSelected.currentEnd.dhstatus = 'tblack';
        $scope.milestoneSelected.currentEnd.optionModel = '';
        $scope.milestoneSelected.currentEnd.snapshot = '';

        $scope.milestoneSelected.actualEnd = {};
        $scope.milestoneSelected.actualEnd.date = '';
        $scope.milestoneSelected.actualEnd.comment = '';
        $scope.milestoneSelected.actualEnd.dhstatus = 'tblack';
        $scope.milestoneSelected.actualEnd.optionModel = '';
        $scope.milestoneSelected.actualEnd.snapshot = '';

        $scope.milestoneSelected.isNew = false;

    };

    $scope.refreshFrontPageMilestone = function() {
        milestoneFactory.getFrontPage($scope.rid, 1)
            .then(function(result) {
                $scope.milestones = result.data;
            });
    };

});