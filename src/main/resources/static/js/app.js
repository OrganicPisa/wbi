/*
 *  Document   : app.js
 *  Author     : Vinh Tran
 *
 */

// Create our angular module
var App = angular.module('app', [
    'ngStorage',
    'ui.router',
    // 'ui.bootstrap',
    'ui-notification',
    'oc.lazyLoad',
    'angular-loading-bar',
    'ngSanitize',
    'ngMaterial'
]);

// Router configuration
App.config(function ($stateProvider, $urlRouterProvider, $locationProvider, $httpProvider, cfpLoadingBarProvider,$qProvider,
                    NotificationProvider, $mdDateLocaleProvider, $mdThemingProvider) {
        $urlRouterProvider.otherwise('/');
    $qProvider.errorOnUnhandledRejections(false);
        var whiteMap = $mdThemingProvider.extendPalette('grey', {'500': '#ffffff', 'contrastDefaultColor': 'dark'});
        $mdThemingProvider.definePalette('white', whiteMap);

        $locationProvider.html5Mode({
            enabled: true,
            requireBase: true
        });
        NotificationProvider.setOptions({
            delay: 1000,
            startTop: 20,
            startRight: 10,
            verticalSpacing: 5,
            horizontalSpacing: 10,
            positionX: 'right',
            positionY: 'bottom',
            maxCount: 3
        });
        cfpLoadingBarProvider.includeSpinner = true;

        $stateProvider
            .state(segmentState)
            .state(internalProgramState)
            .state(customerProgramState)

    }
);

var segmentState = {
    name: 'segment',
    url: '/:status',
    params: {
        status: "active",
    },
    templateUrl: function ($stateParams) {
        return '/segment/index.html'
    },
    controller: 'HomeCtrl',
    resolve: {
        deps: ['$ocLazyLoad', function ($ocLazyLoad) {
            return $ocLazyLoad.load({
                serie: true,
                files: [
                    '/webjars/lr-sticky-header/1.1.0/lrStickyHeader.js',
                    '/webjars/smart-table/2.0.3/smart-table.js',
                    '/webjars/smart-table-sticky-header/1.0.1/stStickyHeader.js',
                    '/webjars/angular-ui-grid/4.0.6/ui-grid.min.css',
                    '/webjars/angular-ui-grid/4.0.6/ui-grid.min.js'
                ]
            });
        }]
    }
};

var internalProgramState = {
    name: 'internalProgram',
    url: '/program/chip/:pid/:rid/:page',
    params: {
        pid: "0",
        rid: "0",
        page: "dashboard",
    },
    templateUrl: function($stateParams){
        return '/program/chip/index.html';
    },
    controller: 'InternalProgramCtrl',
    resolve:{
        deps:['$ocLazyLoad',  function($ocLazyLoad){
            return $ocLazyLoad.load({
                serie: true,
                files : [
                    '/webjars/tinymce/4.2.1/tinymce.min.js',
                    '/webjars/angular-ui-tinymce/0.0.9/src/tinymce.js',
                    '/webjars/smart-table/2.0.3/smart-table.js',
                    '/webjars/smart-table-sticky-header/1.0.1/stStickyHeader.js',
                    '/webjars/angular-xeditable/0.1.9/css/xeditable.css',
                    '/webjars/angular-xeditable/0.1.9/js/xeditable.min.js',
                    '/webjars/angular-ui-grid/4.0.6/ui-grid.min.css',
                    '/webjars/angular-ui-grid/4.0.6/ui-grid.min.js',
                    // '/webjars/angular-confirm/1.2.3/angular-confirm.min.js',
                    '/webjars/FileSaver.js/0.0.2/FileSaver.min.js',
                    '/webjars/highcharts-ng/0.0.11/highcharts-ng.min.js',
                    '/webjars/highcharts/5.0.13/highcharts.js',
                    '/webjars/highcharts/5.0.13/modules/exporting.js',
                    '/webjars/highcharts/5.0.13/modules/offline-exporting.js',
                    '/webjars/highcharts/5.0.13/modules/no-data-to-display.js',
                    '/webjars/angular-file-upload/12.2.13/FileAPI.min.js',
                    '/webjars/angular-file-upload/12.2.13/ng-file-upload-shim.min.js',
                    '/webjars/angular-file-upload/12.2.13/ng-file-upload.min.js'
                ]
            });
        }]
    }
}

var customerProgramState = {
    name: 'customerProgram',
    url: '/program/customer/{pid}/{rid}/{page}',
    params: {
        pid: "0",
        rid: "0",
        page: "dashboard",
    },
    templateUrl: function($stateParams){
        return '/program/customer/index.html';
    },
    controller: 'CustomerProgramCtrl',
    resolve:{
        deps:['$ocLazyLoad',  function($ocLazyLoad){
            return $ocLazyLoad.load({
                serie: true,
                files : [
                    '/webjars/tinymce/4.2.1/tinymce.min.js',
                    '/webjars/angular-ui-tinymce/0.0.9/src/tinymce.js',
                    '/webjars/smart-table/2.0.3/smart-table.js',
                    '/webjars/smart-table-sticky-header/1.0.1/stStickyHeader.js',
                    '/webjars/angular-xeditable/0.1.9/css/xeditable.css',
                    '/webjars/angular-xeditable/0.1.9/js/xeditable.min.js',
                    '/webjars/angular-ui-grid/4.0.6/ui-grid.min.css',
                    '/webjars/angular-ui-grid/4.0.6/ui-grid.min.js',
                    // '/webjars/angular-confirm/1.2.3/angular-confirm.min.js',
                    '/webjars/FileSaver.js/0.0.2/FileSaver.min.js',
                    '/webjars/highcharts-ng/0.0.11/highcharts-ng.min.js',
                    '/webjars/highcharts/5.0.13/highcharts.js',
                    '/webjars/highcharts/5.0.13/modules/exporting.js',
                    '/webjars/highcharts/5.0.13/modules/offline-exporting.js',
                    '/webjars/highcharts/5.0.13/modules/no-data-to-display.js',
                    '/webjars/angular-file-upload/12.2.13/FileAPI.min.js',
                    '/webjars/angular-file-upload/12.2.13/ng-file-upload-shim.min.js',
                    '/webjars/angular-file-upload/12.2.13/ng-file-upload.min.js'
                ]
            });
        }]
    }
}


App.run(function ($rootScope, $http, $localStorage) {
    $rootScope.$on("$stateChangeError", console.log.bind(console));

    $http.get('/api/auth/user').then(function (res) {
        if (typeof res.data != 'undefined' || res.data != '') {
            $rootScope.authenticated = res.data.authenticated;
            $rootScope.loginedUser = res.data.name;
            if (res.data.authorities) {
                angular.forEach(res.data.authorities, function (arr, key) {
                    $rootScope.authority = arr.authority.replace(/^role_/i, "");
                    if (arr.authority.match(/admin/i)) {
                        $rootScope.authority = 'admin';
                    }
                    else {
                        if ($rootScope.authority.match(/view/i))
                            $rootScope.authority = arr.authority;
                    }
                });
            }
        }
    });
    if (!angular.isDefined($localStorage.ipDropDownTemplate)) {
        $http.get('/api/getIPDropDownTemplate').then(function (ret) {
            $localStorage.ipDropDownTemplate = ret;
            $rootScope.ipDropDownTemplate = ret;
        });
    }
    else{
        $rootScope.ipDropDownTemplate = $localStorage.ipDropDownTemplate;
    }

    $rootScope.flaglist = [
        {value: 'On Track (Green/Black)', flag:'black', color:'no-color', order:2},
        {value: 'Pulled In (Green)', flag:'green', color:'green', order:3},
        {value: 'At Risk (Orange)', flag:'orange', color:'orange', order:4},
        {value: 'Delayed (Red)', flag:'red', color:'red', order:5}       ,
        {value: 'Non Active (Grey)', flag:'grey', color:'grey', order:6}
    ];

    if (!navigator.appVersion.match(/chrome/i)) {
        alert("WBI only supports Chrome. Please use it when accessing WBI");
    }
    $rootScope.tinymceOptions = {
        mode:'textarea',
        plugins: [ "table", "textcolor" , "autoresize"],
        menubar:false,
        statusbar: false,
        inline: false,
        width: '100%',
        height: 400,
        autoresize_min_height: 400,
        entity_encoding: "numeric",
        browser_spellcheck : true ,
        toolbar1: "bold italic underline strikethrough | alignleft aligncenter alignright alignjustify |  bullist numlist outdent indent | forecolor backcolor fontsizeselect fontselect | table ",
        skin: 'lightgray',
        theme : 'modern'
    };
});

App.filter('filterStartsWith', function () {
    return function (items, prefix, itemProperty) {
        return items.filter(function (item) {
            var findIn = itemProperty ? item[itemProperty] : item;
            return findIn.toString().indexOf(prefix) === 0;
        });
    };
});


// Application Main Controller
App.controller('HeaderCtrl', function ($scope, $rootScope, $window, $http, $localStorage, $mdBottomSheet, $mdSidenav) {
    $scope.environmentMode = '';
    var hostname = window.location.hostname;
    if (hostname.match(/qa/i)) {
        $scope.environmentMode = 'QA';
    }
    else if (hostname.match(/localhost/i)) {
        $scope.environmentMode = 'DEV';
    }
    var searchProgramDisplay = [];
    $scope.searchProgram = function (term) {
        searchProgramDisplay = [];
        return $http.get('/api/search/program', {
            params: {
                'term': term.toLowerCase().replace(/\+/i, 'plus')
            }
        }).then(function (res) {
            searchProgramDisplay = [];
            searchProgramDisplay = res.data;
            return searchProgramDisplay;
        });
    };
    $scope.goLogout = function () {
        window.location = "/logout";
    }
    $scope.clearAllCache = function () {
        $http.get('/admin/clearCache')
            .then(function (ret) {
                location.reload();
            });
    }
    $scope.selectedItemChange = function (item) {
        if (typeof item.url != 'undefined') {
            window.location = item.url;
        }
    }
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
