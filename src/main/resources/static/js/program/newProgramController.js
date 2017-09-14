App.controller('NewProgramCtrl', function ($scope, $rootScope, $http, $sce, $stateParams, $filter, $location, blockUI,
                                           $timeout, $localStorage, Notification, $mdDialog, $mdSidenav) {
    $scope.page = $stateParams.page;
    $scope.chip = {};
    $scope.customer = {};
    $scope.ip = {};
    $scope.ip.info = {};
    $scope.rev = {};
    $scope.rev.oldRev = '';
    $scope.createNew = {};
    $scope.segments = [];
    $scope.segmentSelected = {};

    var searchProgramDisplay = [];
    $scope.searchProgram = function (term, type) {
        searchProgramDisplay = [];
        return $http.get('/api/search/program', {
            params: {
                'term': term.toLowerCase().replace(/\+/i, 'plus'),
                'type': type
            }
        }).then(function (res) {
            searchProgramDisplay = [];
            searchProgramDisplay = res.data;
            return searchProgramDisplay;
        });
    };
    $scope.selectedProgramChange = function (item) {
        $scope.rev.pname = item.pname;
        $scope.rev.pid = item.pid;
        $scope.revisionList = [];
        $http.get('/api/program/getRevisionList?pid=' + $scope.rev.pid)
            .then(function (ret) {
                $scope.revisionList = ret.data;
            });
    };

    $http.get('/api/segment/getActiveSegments')
        .then(function (ret) {
            for (var i = 0; i < ret.data.length; i++) {
                if (!ret.data[i].name.match(/^(customer|serdes|software|ip)/i)) {
                    $scope.segments.unshift(ret.data[i]);
                }
            }
        });

    $scope.submitChipForm = function (isValid) {
        if (isValid) {
            blockUI.message("Please wait...");

            $http.post("/api/program/new", {'data': $scope.chip, 'type': 'chip'})
                .then(function (ret) {
                    if (ret.data.pid) {
                        blockUI.message("Generating project revision....");
                        $scope.chip.pid = ret.data.pid;
                        $timeout(function () {
                            blockUI.message("The whole process may take up to 5 minutes. Please do not refresh the page");
                        }, 30000);
                        $timeout(function () {
                            blockUI.message("Almost there. Cleanning up....");
                        }, 60000);
                        $http.post("/api/revision/new", {'data': $scope.chip, 'type': 'chip'})
                            .then(function (ret) {
                                if (ret.data.url) {
                                    window.location = ret.data.url;
                                }
                            });
                    }
                    else if (ret.data.error) {
                        console.log(ret.data.error);

                    }
                });
        }
    };

    $scope.duplicateRev = false;
    $scope.checkDuplicateRevision = function (checkRev) {
        $scope.duplicateRev = false;
        if (typeof checkRev.rname != 'undefined' && checkRev.rname.length > 1) {
            var rev = $filter('filter')($scope.revisionList, {'revision': checkRev.rname});
            if (rev.length > 0) {
                Notification.error({
                    title: 'Duplicate Revision', message: 'Revision already created',
                    positionY: 'top', positionX: 'center', delay: '5000', replaceMessage: true
                });
                $scope.duplicateRev = true;
            } else {
                Notification.clearAll();
            }
        }
    };

    $scope.submitRevisionForm = function (isValid) {
        if (isValid && !$scope.duplicateRev) {
            blockUI.message("Please wait...");
            $timeout(function () {
                blockUI.message("The whole process may take up to 5 minutes. Please do not refresh the page");
            }, 30000);
            $timeout(function () {
                blockUI.message("Almost there. Cleanning up....");
            }, 60000);

            $http.post("/api/revision/new", {'data': $scope.rev, 'type': 'chip'})
                .then(function (ret) {
                    if (ret.data.url) {
                        window.location = ret.data.url;
                    }
                });
        }
    };
    $scope.submitCustomerForm = function (isValid) {
        if (isValid) {
            $http.post("/api/program/new", {'data': $scope.customer, 'type': 'customer'})
                .then(function (ret) {
                    if (ret.data.pid) {
                        blockUI.message("Generating project revision....");
                        $scope.customer.pid = ret.data.pid;
                        $timeout(function () {
                            blockUI.message("The whole process may take up to 5 minutes. Please do not refresh the page");
                        }, 30000);
                        $timeout(function () {
                            blockUI.message("Almost there. Cleanning up....");
                        }, 60000);
                        $http.post("/api/revision/new", {'data': $scope.customer, 'type': 'customer'})
                            .then(function (ret) {
                                if (ret.data.url) {
                                    window.location = ret.data.url;
                                }
                            });
                    }
                    else if (ret.data.error) {
                        console.log(ret.data.error);

                    }
                });
        }
    };

    $scope.submitIPForm = function (isValid) {
        if (isValid) {
            blockUI.message("Please wait...");
            var pname = $scope.ip.pname;
            var category = $scope.ip.info.category;
            if (pname.toLowerCase().indexOf(category.toLowerCase()) == -1) {
                $scope.ip.pname = $scope.ip.info.category + " " + $scope.ip.pname;
            }
            $http.post("/api/program/new", {'data': $scope.ip, 'type': 'ipprogram'})
                .then(function (ret) {
                    if (ret.data.pid) {
                        blockUI.message("Generating project revision....");
                        $scope.ip.pid = ret.data.pid;
                        $timeout(function () {
                            blockUI.message("The whole process may take up to 5 minutes. Please do not refresh the page");
                        }, 30000);
                        $timeout(function () {
                            blockUI.message("Almost there. Cleanning up....");
                        }, 60000);
                        $http.post("/api/revision/new", {'data': $scope.ip, 'type': 'iprevision'})
                            .then(function (ret) {
                                if (ret.data.url) {
                                    window.location = ret.data.url;
                                }
                            });
                    }
                    else if (ret.data.error) {
                        console.log(ret.data.error);

                    }
                });
        }
    };
    $scope.selectedIPProgramChange = function (item) {
        $scope.ip.pname = item.pname;
        $scope.ip.pid = item.pid;

        $scope.revisionList = [];
        $http.get('/api/program/getRevisionList?pid=' + $scope.ip.pid)
            .then(function (ret) {
                $scope.revisionList = ret.data;
            });
    };
    $scope.submitIPRevForm = function (isValid) {
        if (isValid) {
            blockUI.message("Generating project revision....");
            $timeout(function () {
                blockUI.message("The whole process may take up to 5 minutes. Please do not refresh the page");
            }, 30000);
            $timeout(function () {
                blockUI.message("Almost there. Cleanning up....");
            }, 60000);
            $http.post("/api/revision/new", {'data': $scope.ip, 'type': 'iprevision'})
                .then(function (ret) {
                    if (ret.data.url) {
                        window.location = ret.data.url;
                    }
                });
        }

    }
    // else if($scope.page.match(/select/i)){
    //     $scope.newGo = function(v){
    //         var split = window.location.href.split("/");
    //         var x = split.slice(0, split.length - 1).join("/") + "/";
    //         window.location = x+v;
    //     }
    // }
    // else if($scope.page.match(/internalprogram/i)){
    //     $scope.submitChipForm = function(isValid){
    //         if(isValid){
    //             Notification.info({title: 'Please Wait', message: 'Your project is being generated. The whole process may take up to 5 minutes. Please do not refresh the page',
    //                 positionY: 'top', positionX: 'center', delay: '30000'});
    //             $http.post("/program/addNew", {'data':$scope.chip, 'type': 'chip'})
    //                 .then(function(ret){
    //                     if(ret['url']){
    //                         Notification.success({message: 'Project was created. You will be redirected in 5 sec',
    //                             positionY: 'top', positionX: 'center', delay: '5000', replaceMessage:true});
    //                         $timeout(function(){
    //                             window.location = ret['url'];
    //                         }, 5000);
    //                     }
    //                 });
    //         }
    //     };
    // }
    // else if($scope.page.match(/ipprogram/i)){
    //     $scope.submitIPForm = function(isValid){
    //         if(isValid){
    //             Notification.info({title: 'Please Wait', message: 'Your project is being generated. The whole process may take up to 5 minutes. Please do not refresh the page',
    //                 positionY: 'top', positionX: 'center', delay: '30000'});
    //             var pname = $scope.ip.pname;
    //             var category = $scope.ip.info.category;
    //             if(pname.toLowerCase().indexOf(category.toLowerCase()) == -1){
    //                 $scope.ip.pname = $scope.ip.info.category + " "+$scope.ip.pname;
    //             }
    //             $http.post("/program/addNew", {'data':$scope.ip, 'type': 'ipprogram'})
    //                 .then(function(ret){
    //                     if(ret['url']){
    //                         Notification.success({message: 'Project was created. You will be redirected in 5 sec',
    //                             positionY: 'top', positionX: 'center', delay: '5000', replaceMessage:true});
    //                         $timeout(function(){
    //                             window.location = ret['url'];
    //                         }, 5000);
    //                     }
    //                 });
    //         }
    //     };
    // }
    // else if($scope.page.match(/iprevision/i)){
    //     $scope.onProjectSelect= function($item, $model, $label){
    //         $scope.ip.pname = $label.replace(/<(?:.|\n)*?>/gm, '');
    //         $scope.ip.pid = $item.url.split(/\//)[3];
    //         $http.get('/program/getRevisionList?pid='+$scope.ip.pid)
    //             .then(function(ret){
    //                 $scope.revisionList = ret;
    //             });
    //     };
    //     $scope.duplicateRev = false;
    //     $scope.checkRevision= function(){
    //         $scope.duplicateRev = false;
    //         if(typeof $scope.ip.rname != 'undefined' && $scope.ip.rname.length >1){
    //             var ip = $filter('filter')($scope.revisionList, {'revision':$scope.ip.rname});
    //             if(ip.length >0){
    //                 Notification.error({title: 'Duplicate Revision Name', message: 'Revision already created',
    //                     positionY: 'top', positionX: 'center', delay: '5000', replaceMessage:true});
    //                 $scope.duplicateRev = true;
    //             }else{
    //                 Notification.clearAll();
    //             }
    //         }
    //     };
    //     $scope.submitIPRevForm = function(isValid){
    //         if(isValid && !$scope.duplicateRev){
    //             Notification.info({title: 'Please Wait', message: 'Your revision is being generated. The whole process may take up to 5 minutes. Please do not refresh the page',
    //                 positionY: 'top', positionX: 'center', delay: '30000'});
    //             $http.post("/program/addNew", {'data':$scope.ip, 'type': 'iprev'})
    //                 .then(function(ret){
    //                     if(ret['url']){
    //                         Notification.then({message: 'Project was created. You will be redirected in 5 sec',
    //                             positionY: 'top', positionX: 'center', delay: '5000', replaceMessage:true});
    //                         $timeout(function(){
    //                             window.location = ret['url'];
    //                         }, 5000);
    //                     }
    //                 });
    //         }
    //     };
    // }
});