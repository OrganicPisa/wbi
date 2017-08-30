// Header Controller
App.controller('HeaderCtrl', function ($scope, $rootScope, $timeout, $window, $http, $location) {
    $scope.environmentMode = '';
    var hostname = window.location.hostname;
    if (hostname.match(/qa/i)) {
        $scope.environmentMode = 'QA';
    }
    else if (hostname.match(/localhost/i)) {
        $scope.environmentMode = 'DEV';
    }
    $scope.searchProgram = function (term) {
        $http.get('/api/search/program?term=' + term.toLowerCase().replace(/\+/i, 'plus')
        ).then(function (res) {
            return res.data;
        });
    };
    // $rootScope.authenticated = false;
    // $http.get("/api/auth/user", {ignoreLoadingBar: true})
    //     .then(function (response) {
    //         $rootScope.authenticated = response.authenticated;
    //         if (response.principal && response.principal.authorities) {
    //             $rootScope.username = response.principal.username;
    //             angular.forEach(ret.principal.authorities, function (arr, key) {
    //                 $rootScope.authority = arr.authority.replace(/^role_/i, "");
    //                 if (arr.authority.match(/admin/i)) {
    //                     $rootScope.authority = 'admin';
    //                 }
    //                 else {
    //                     if ($rootScope.authority.match(/view/i))
    //                         $rootScope.authority = arr.authority;
    //                 }
    //             });
    //         }
    //     });


    $scope.goLogout = function () {
        $window.location = "/logout";
    };
    $scope.clearAllCache = function () {
        $http.get('/api/admin/clearCache')
            .success(function (ret) {
                location.reload();
            });
    };
    $scope.changeHeaderClass = function () {
        var carr = document.getElementsByClassName("nav-header");
        if (carr.length > 0) {
            for (var i = 0; i < carr.length; i++) {
                var self = angular.element(carr[i]);
                var liarr = self.children();
                if (self.hasClass('headerResponsive')) {
                    self.removeClass('headerResponsive');
                    if (liarr.length > 0) {
                        for (var j = 0; j < liarr.length; j++) {
                            var li = angular.element(liarr[j]);
                            if (!li.hasClass('hidden-sm') && !li.hasClass('hidden-md')) {
                                li.addClass('hidden-sm hidden-xs');
                            }
                        }
                    }
                }
                else {
                    self.addClass('headerResponsive');
                    if (liarr.length > 0) {
                        for (var j = 0; j < liarr.length; j++) {
                            var li = angular.element(liarr[j]);
                            if (li.hasClass('hidden-sm') && !li.hasClass('header-search') && !li.hasClass('admin-header-menu')) {
                                li.removeClass('hidden-sm hidden-xs');
                            }
                        }
                    }
                }

            }
        }
    };
    $scope.openNewTab = function (url) {
        $window.open(url, '_blank');
    };
    $scope.onSelect = function ($item, $model, $label) {
        window.location = $item.url + "/dashboard";
    };
});