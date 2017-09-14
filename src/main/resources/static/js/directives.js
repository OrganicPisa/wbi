App.directive('searchWatchModel', function () {
    return {
        require: '^stTable',
        scope: {
            searchWatchModel: '='
        },
        link: function (scope, ele, attr, ctrl) {
            var table = ctrl;
            scope.$watch('searchWatchModel', function (val) {
                ctrl.search(val);
            });
        }
    };
});

App.filter('split', function () {
    return function (input, splitChar, splitIndex) {
        return input.split(splitChar)[splitIndex];
    }
});

App.filter('formatResourceSkillName', function () {
    return function (input) {
        if (input) {
            return input.replace(/^z/ig, '');
        }
    }
});

App.filter("myUpperCase", function () {
    return function (text) {
        if (text != null && typeof text != 'undefined') {
            text = text.trim();
            var str = text.replace(/plus/ig, '+').replace("(", " ( ").replace(
                ")", " ) ").replace("/", " / ");
            if (str.replace(/[a-zA-Z]/gi, "").replace(/[\])}[{(]/g, '')
                    .replace(/\s/g, '').length > 2) {
                return str.toUpperCase();
            } else {
                var arr = str.split(/(\_|\s|\-|\/)/g);
                var ret = "";
                for (var i = 0; i < arr.length; i++) {
                    if (arr[i].match(/(ver)/i)) {
                        ret += arr[i].charAt(0).toUpperCase() + arr[i].slice(1)
                            + " ";
                    } else if (arr[i].replace(/[0-9]/g, '').length < 4
                        && !arr[i].match(/(key|die)/i)) {
                        ret += arr[i].toUpperCase() + " ";
                    } else {
                        ret += arr[i].charAt(0).toUpperCase() + arr[i].slice(1)
                            + " ";
                    }
                }
                ret = ret.trim();
                return ret.replace(" ( ", "(").replace(" ) ", ")");
            }
        }
    };
});

App.directive('ngUniqueProgram', function ($http) {
    return {
        require: 'ngModel',
        link: function (scope, elem, attrs, ctrl) {
            elem.on('blur', function (e) {
                scope.$apply(function () {
                    if (elem.val().toLowerCase() == 'na'
                        && elem[0].name.match(/basedie/i)) {
                        ctrl.$setValidity('programUnique', true);
                    } else {
                        $http.get('/api/program/checkExist?program=' + elem.val().replace(/\+/g, 'plus').toLowerCase())
                            .then(function (data, status, headers, config) {
                                ctrl.$setValidity('programUnique', !data.data.ret);
                            })
                    }
                })
            });
        }
    }
});

App.directive("dynamic", function ($compile, $sce) {
    return {
        restrict: "A",
        replace: true,
        transclude: false,
        link: function (scope, ele, attrs) {
            scope.$watch(attrs.dynamic, function (html) {
                ele.html($sce.trustAsHtml(html));
                $compile(ele.contents())(scope);
            })
        }
    }
});


App.directive('headlineEditForm', function () {
    return {
        restrict: 'ACE',
        template: "<form editable-form name='headlineeditform' onaftersave='saveHeadline()' oncancel='cancel()' ng-show='headlineeditform.$visible'>"
        + "<div class='btn-form'>"
        + "<button type='submit' ng-disabled='headlineeditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
        + "<a ng-disabled='headlineeditform.$waiting' ng-click='headlineeditform.$cancel();' class='btn btn-xs pull-right'>Cancel</a></div>"
        + "<div class='row'><div class='form-group col-sm-12'><label for='headlineSection.status' class='control-label'>Status</label>"
        + "<span editable-textarea='headlineSection.status' e-rows='5'></span></div></div>"
        + "<div class='row'><div class='form-group col-sm-12'><label for='headlineSection.issue' class='control-label'>Issue</label>"
        + "<span editable-textarea='headlineSection.issue' e-rows='5'></span></div></div>"
        + "<div class='row'><div class='form-group col-sm-8'>	<label for='headlineSection.nextStep1' class='control-label'>Next Step 1</label>"
        + "<span editable-textarea='headlineSection.nextStep1' e-rows='3'></span></div>"
        + "<div class='form-group col-sm-2'>"
        + "<label for='headlineSection.dept1' class='control-label'>Department</label><span editable-text='headlineSection.dept1'></span></div>"
        + "<div class='form-group col-sm-2'>"
        + "<label for='headlineSection.owner1' class='control-label'>Owner</label><span editable-text='headlineSection.owner1'></span></div></div>"
        + "<div class='row'><div class='form-group col-sm-8'>"
        + "<label for='headlineSection.nextStep2' class='control-label'>Next Step 2</label>"
        + "<span editable-textarea='headlineSection.nextStep2' e-rows='3'></span></div>"
        + "<div class='form-group col-sm-2'>"
        + "<label for='headlineSection.dept2' class='control-label'>Department</label><span editable-text='headlineSection.dept2'></span></div>"
        + "<div class='form-group col-sm-2'>"
        + "<label for='headlineSection.owner2' class='control-label'>Owner</label><span editable-text='headlineSection.owner2'></span></div></div>"
        + "<div class='row'><div class='form-group col-sm-8'><label for='headlineSection.nextStep3' class='control-label'>Next Step 3</label>"
        + "<span editable-textarea='headlineSection.nextStep3' e-rows='3'></span></div>"
        + "<div class='form-group col-sm-2'><label for='headlineSection.dept3' class='control-label'>Department</label><span editable-text='headlineSection.dept3'></span></div>"
        + "<div class='form-group col-sm-2'><label for='headlineSection.owner3' class='control-label'>Owner</label>"
        + "<span editable-text='headlineSection.owner3'></span></div></div></form>"
    }
});

App
    .directive(
        'ipCatHeadlineEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='ipcatheadlineeditform' onaftersave='saveIPCatHeadline()' oncancel='cancel()' ng-show='ipcatheadlineeditform.$visible'>"
                + "<div class='btn-form'>"
                + "<button type='submit' ng-disabled='ipcatheadlineeditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a ng-disabled='ipcatheadlineeditform.$waiting' ng-click='ipcatheadlineeditform.$cancel();' class='btn btn-xs pull-right'>Cancel</a></div>"
                + "<div class='row'><div class='form-group col-sm-12'><label for='ipCatHeadlineSection.status' class='control-label'>Status</label>"
                + "<span editable-textarea='ipCatHeadlineSection.status' e-rows='5'></span></div></div>"
                + "<div class='row'><div class='form-group col-sm-12'><label for='ipCatHeadlineSection.issue' class='control-label'>Issue</label>"
                + "<span editable-textarea='ipCatHeadlineSection.issue' e-rows='5'></span></div></div>"
                + "<div class='row'><div class='form-group col-sm-8'>	<label for='ipCatHeadlineSection.nextStep1' class='control-label'>Next Step 1</label>"
                + "<span editable-textarea='ipCatHeadlineSection.nextStep1' e-rows='3'></span></div>"
                + "<div class='form-group col-sm-2'>"
                + "<label for='ipCatHeadlineSection.dept1' class='control-label'>Department</label><span editable-text='ipCatHeadlineSection.dept1'></span></div>"
                + "<div class='form-group col-sm-2'>"
                + "<label for='ipCatHeadlineSection.owner1' class='control-label'>Owner</label><span editable-text='ipCatHeadlineSection.owner1'></span></div></div>"
                + "<div class='row'><div class='form-group col-sm-8'>"
                + "<label for='ipCatHeadlineSection.nextStep2' class='control-label'>Next Step 2</label>"
                + "<span editable-textarea='ipCatHeadlineSection.nextStep2' e-rows='3'></span></div>"
                + "<div class='form-group col-sm-2'>"
                + "<label for='ipCatHeadlineSection.dept2' class='control-label'>Department</label><span editable-text='ipCatHeadlineSection.dept2'></span></div>"
                + "<div class='form-group col-sm-2'>"
                + "<label for='ipCatHeadlineSection.owner2' class='control-label'>Owner</label><span editable-text='ipCatHeadlineSection.owner2'></span></div></div>"
                + "<div class='row'><div class='form-group col-sm-8'><label for='ipCatHeadlineSection.nextStep3' class='control-label'>Next Step 3</label>"
                + "<span editable-textarea='ipCatHeadlineSection.nextStep3' e-rows='3'></span></div>"
                + "<div class='form-group col-sm-2'><label for='ipCatHeadlineSection.dept3' class='control-label'>Department</label><span editable-text='ipCatHeadlineSection.dept3'></span></div>"
                + "<div class='form-group col-sm-2'><label for='ipCatHeadlineSection.owner3' class='control-label'>Owner</label>"
                + "<span editable-text='ipCatHeadlineSection.owner3'></span></div></div></form>"
            }
        });
App
    .directive(
        'ipSettingEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='ipsettingeditform' onaftersave='saveSettings()' oncancel='cancel()'>"
                + "<div class='btn-form' ng-show='ipsettingeditform.$visible'>"
                + "<button type='submit' ng-disabled='ipsettingeditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a ng-disabled='ipsettingeditform.$waiting' ng-click='ipsettingeditform.$cancel();' class='btn btn-xs pull-right'>Cancel</a></div>"
                + "<table class='table table-condensed table-bordered'>"
                + "<tbody>"
                + "<tr ng-show='ipsettingeditform.$visible'><td><strong>Program</strong></td>"
                + "<td><span editable-text='settings.program' e-form='ipsettingeditform' >{{settings.program}}</span></td></tr>"
                + "<tr ng-show='ipsettingeditform.$visible'><td><strong>Revision</strong></td>"
                + "<td><span editable-text='settings.revision' e-form='ipsettingeditform' >{{settings.revision}}</span></td></tr>"
                + "<tr><td><strong>Stage</strong></td>"
                + "<td><span editable-select='settings.stage' e-form='ipsettingeditform' e-ng-options='key for (key, value) in ipselecttemplate.stage'>{{settings.stage}}</span></td></tr>"
                + "<tr><td><strong>Status</strong></td>"
                + "<td><span editable-select='settings.status' e-form='ipsettingeditform' e-ng-options='key for (key, value) in ipselecttemplate.status'>{{settings.status}}</span></td></tr>"
                + "<tr><td><strong>Schedule Temperature</strong></td>"
                + "<td><span editable-select='settings.scheduleFlag' e-form='ipsettingeditform' e-ng-options='item.value for item in flaglist'>{{settings.scheduleFlag.value}}</span></td></tr>"
                + "</tbody></table></form>"
            }
        });

App
    .directive(
        'meetingEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='meetingeditform' onaftersave='saveMeetingTable()' oncancel='cancel()'>"
                + "<div class='btn-form' ng-show='meetingeditform.$visible'>"
                + "<a ng-disabled='meetingeditform.$waiting' ng-click='addNewLink(meetings)' class='btn btn-xs btn-link pull-right'>New</a>"
                + "<button type='submit' ng-disabled='meetingeditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a ng-disabled='meetingeditform.$waiting' ng-click='meetingeditform.$cancel(); cancelSaveMeeting()' class='btn btn-xs pull-right'>Cancel</a></div>"
                + "<table class='table table-condensed table-bordered' >"
                + "<tbody><tr ng-repeat='meeting in meetings | filter:filterLink'><td style='word-wrap:break-word;'>"
                + "<strong editable-text='meeting.key' e-form='meetingeditform'>{{ meeting.key | myUpperCase }}</strong></td>"
                + "<td style='word-wrap:break-word;'>"
                + "<label class='control-label'  ng-show='meetingeditform.$visible'>Name: </label>"
                + "<span editable-text='meeting.name' e-form='meetingeditform' ng-show='meetingeditform.$visible'></span>"
                + "<label class='control-label'  ng-show='meetingeditform.$visible'>URL :</label>"
                + "<a ng-href='{{meeting.url}}' editable-url='meeting.url' e-form='meetingeditform'  target='_blank'>{{ meeting.name | myUpperCase }}</a></td>"
                + "<td  ng-show='meetingeditform.$visible'><a type='button' ng-click='deleteMeeting(meeting.id)' class='btn btn-xs pull-right'><strong class='text-danger'>Delete</strong></a></td></tr>"
                + "</tbody></table></form>"
            }
        });
App
    .directive(
        'skuEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='skueditform' onaftersave='saveSkuTable()' oncancel='cancel()'>"
                + "<div class='btn-form' ng-show='skueditform.$visible'>"
                + "<a ng-disabled='skueditform.$waiting' ng-click='addNewSku(skus)' class='btn btn-xs btn-link pull-right'>New</a>"
                + "<button type='submit' ng-disabled='skueditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a ng-disabled='skueditform.$waiting' ng-click='skueditform.$cancel(); cancelSaveSku()' class='btn btn-xs pull-right'>Cancel</a></div>"
                + "<table class='table table-condensed table-bordered table-header-bg' >"
                + "<thead><tr><th class='theader col-md-1'>SKU #</th><th class='theader col-md-1'>AKA</th>"
                + "<th class='theader col-md-1'>Date Available</th>"
                + "<th class='theader col-md-2'>Description</th>"
                + "<th class='theader col-md-1'>i-temp</th>"
                + "<th class='theader col-md-1'>Core Freq</th>"
                + "<th class='theader col-md-1'>Port Config</th>"
                + "<th class='theader col-md-1'>Io Capacity</th>"
                + "<th class='theader col-md-1'>Serdes</th>"
                + "<th class='theader col-md-1' ng-show='skueditform.$visible'>Action</th></tr></thead>"
                + "<tbody><tr ng-repeat='sku in skus | filter:filterSku | orderBy:skuOrder'>"
                + "<td><span editable-text='sku.num' e-form='skueditform'>{{ sku.num }}</span></td>"
                + "<td><span editable-text='sku.aka' e-form='skueditform'>{{ sku.aka }}</span></td>"
                + "<td><span editable-text='sku.dateAvailable' e-form='skueditform'>{{ sku.dateAvailable }}</span></td>"
                + "<td><span editable-text='sku.desc' e-form='skueditform'>{{ sku.desc }}</span></td>"
                + "<td><span editable-text='sku.itemp' e-form='skueditform'>{{ sku.itemp }}</span></td>"
                + "<td><span editable-text='sku.frequency' e-form='skueditform'>{{ sku.frequency }}</span></td>"
                + "<td><span editable-text='sku.portConfig' e-form='skueditform'>{{ sku.portConfig }}</span></td>"
                + "<td><span editable-text='sku.io' e-form='skueditform'>{{ sku.io | myUpperCase }}</span></td>"
                + "<td><span editable-text='sku.serdes' e-form='skueditform'>{{ sku.serdes | myUpperCase }}</span></td>"
                + "<td  ng-show='skueditform.$visible'><a ng-click='deleteSku(sku.id)' class='btn btn-xs pull-left'><strong class='text-danger'>Delete</strong></a></td></tr>"
                + "</tbody></table></form>"
            }
        });
App
    .directive(
        'swEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='sweditform' onaftersave='saveSwTable()' oncancel='cancel()'>"
                + "<div class='btn-form' ng-show='sweditform.$visible'>"
                + "<a ng-disabled='sweditform.$waiting' data-ng-click='addNewSw(sws)' class='btn btn-xs pull-right'>New</a>"
                + "<button type='submit' ng-disabled='sweditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a ng-disabled='sweditform.$waiting' ng-click='sweditform.$cancel(); cancelSaveSw()' class='btn btn-xs pull-right'>Cancel</a></div>"
                + "<table class='table table-condensed table-bordered table-header-bg'>"
                + "<thead><tr><th class='theader' style='width:1%;'>S</th>"
                + "<th class='theader'  style='width:8%;' ng-show='sweditform.$visible'>Status</th>"
                + "<th class='theader'  style='width:13%;'>Version</th>"
                + "<th class='theader'  style='width:15%;' >EA</th>"
                + "<th class='theader'  style='width:15%;' >GA</th>"
                + "<th class='theader'  style='width:68%;' >Headline</th>"
                + "<th class='theader' style='width:10%;'  ng-show='sweditform.$visible'>In Report</th></tr></thead>"
                + "<tbody><tr ng-repeat='sw in sws | filter:filterSw | orderBy:[swo.predicate1, swo.predicate2]'>"
                + "<td ng-class='(sw.color|lowercase)'></td>"
                + "<td  ng-show='sweditform.$visible'>"
                + "<div class='btn-group btn-group-sm'>"
                + "<button type='button' class='btn btn-danger' title='Delayed' ng-model='milestoneStatus' value='red' ng-click=(sw.color='red')></button>"
                + "<button type='button' class='btn btn-warning' title='At Risk' ng-model='milestoneStatus' value='orange' ng-click=(sw.color='orange')></button>"
                + "<button type='button' class='btn btn-success' title='Pull Back'  ng-model='milestoneStatus' value='green' ng-click=(sw.color='green')></button>"
                + "</div></td>"
                + "<td> <b editable-text='sw.rname' e-form='sweditform'>{{ sw.rname }}</b></td>"
                + "<td> <span e-ng-disabled='sw.headProgram' editable-text='sw.ea' e-form='sweditform'>{{sw.ea }}</span></td>"
                + "<td> <span e-ng-disabled='sw.headProgram' editable-text='sw.ga' e-form='sweditform'>{{sw.ga }}</span></td>"
                + "<td><p>[{{sw.ts}}]</p>"
                + "<span editable-textarea='sw.headline' e-rows='7' e-cols='40' e-form='sweditform'><pre>{{ sw.headline }}</pre></span>"
                + "</td>"
                + "<td style='text-align:center'  ng-show='sweditform.$visible'><span editable-checkbox='sw.includeReport' "
                + "e-ng-disabled='sw.headProgram' e-title='Include In Report?' >{{ sw.includeReport && 'Yes' || 'No' }} </span></td></tr>"
                + "</tbody></table></form>"
            }
        });

App
    .directive(
        'linkEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='linkeditform' onaftersave='saveLinkTable()' oncancel='cancel()'>"
                + "<div class='btn-form' ng-show='linkeditform.$visible'>"
                + "<a ng-disabled='linkeditform.$waiting' ng-click='addNewLink(links)' class='btn btn-xs pull-right'>New</a>"
                + "<button type='submit' ng-disabled='linkeditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a ng-disabled='linkeditform.$waiting' ng-click='linkeditform.$cancel(); cancelSaveLink()' class='btn btn-xs pull-right'>Cancel</a></div>"
                + "<table class='table table-condensed table-bordered' >"
                + "<tbody><tr ng-repeat='link in links | filter:filterLink'><td style='word-wrap:break-word;'>"
                + "<strong editable-text='link.key' e-form='linkeditform'>{{ link.key | myUpperCase }}</strong></td>"
                + "<td style='word-wrap:break-word;'>"
                + "<label class='control-label'  ng-show='linkeditform.$visible'>Name: </label>"
                + "<span editable-text='link.name' e-form='linkeditform' ng-show='linkeditform.$visible'></span>"
                + "<label class='control-label'  ng-show='linkeditform.$visible'>URL: </label>"
                + "<a ng-href='{{link.url}}' editable-url='link.url' e-form='linkeditform'  target='_blank'>{{ link.name || 'url' }}</a></td>"
                + "<td  ng-show='linkeditform.$visible'><a ng-click='deleteLink(link.id)' class='btn btn-xs pull-right'><strong class='text-danger'>Delete</strong></a></td></tr>"
                + "</tbody></table></form>"
            }
        });

App
    .directive(
        'outlookEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='outlookeditform' onaftersave='saveOutlook(outlook)' oncancel='cancel()'>"
                + "<span editable-textarea='outlook' e-form='outlookeditform'>{{ outlook || 'No Outlook'}}</span>"
                + "<div ng-show='outlookeditform.$visible' >"
                + "<md-button  md-no-ink  type='submit' ng-disabled='outlookeditform.$waiting'>Save</md-button>"
                + "<md-button md-no-ink ng-disabled='outlookeditform.$waiting' ng-click='outlookeditform.$cancel();'>Cancel</md-button></div>"
                + "</form>"
            }
        });
App
    .directive(
        'informationEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='infoeditform' onaftersave='saveInfoDashboard()' oncancel='cancel()'>"
                + "<div class='btn-form' ng-show='infoeditform.$visible'>"
                + "<button type='submit' ng-disabled='infoeditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a ng-disabled='infoeditform.$waiting' ng-click='infoeditform.$cancel(); cancelSaveInfoDashboard();' class='btn btn-xs pull-right'>Cancel</a></div>"
                + "<table class='table table-condensed table-bordered' >"
                + "<tbody>"
                + "<tr><td><strong>Program Name</strong></td><td><span>{{informationDashboard.program_name}}</span></td></tr>"
                + "<tr data-ng-if='isCustomerProgram'><td><strong>Customer Name</strong></td><td><span>{{informationDashboard.customer_name}}</span></td></tr>"
                + "<tr data-ng-if='isChipProgram'><td><strong>Base Die</strong></td><td><span>{{informationDashboard.base_die}}</span></td></tr>"
                + "<tr data-ng-if='isChipProgram'><td><strong>Segment</strong></td><td><span>{{informationDashboard.segment}}</span></td></tr>"
                + "<tr ng-repeat='info in informationDashboard.data | orderBy:[infopredicate1, infopredicate2] | filter:filterInfo'>"
                + "<td><strong editable-text='info.key' e-form='infoeditform' edit-disabled='true'>{{ info.key }}</strong></td>"
                + "<td><span editable-text='info.value' e-form='infoeditform' edit-disabled='!info.editable' >{{ info.value }}</span></td>"
                + "</tr>"
                + "</tbody></table></form>"
            }
        });

App
    .directive(
        'ipInformationEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='infoeditform' onaftersave='saveInfoDashboard()' oncancel='cancel()'>"
                + "<div class='btn-form' ng-show='infoeditform.$visible'>"
                + "<a ng-disabled='infoeditform.$waiting' ng-click='addNewInfoDashboard(informationDashboard)' class='btn btn-xs pull-right'>New</a>"
                + "<button type='submit' ng-disabled='infoeditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a ng-disabled='infoeditform.$waiting' ng-click='infoeditform.$cancel(); cancelSaveInfoDashboard();' class='btn btn-xs pull-right'>Cancel</a></div>"
                + "<table class='table table-condensed table-bordered' >"
                + "<tbody>"
                + "<tr><td><strong>Program Name</strong></td><td><span>{{informationDashboard.program_name}}</span></td></tr>"
                + "<tr><td><strong>Category</strong></td>"
                + "<td><span editable-select='informationDashboard.category.value' e-form='infoeditform' e-ng-options='key for (key, value) in ipselecttemplate.category'>{{informationDashboard.category.value}}</span></td></tr>"
                + "<tr><td><strong>Technology</strong></td>"
                + "<td><span editable-select='informationDashboard.technology.value' e-form='infoeditform' e-ng-options='key for (key, value) in ipselecttemplate.technology'>{{informationDashboard.technology.value}}</span></td></tr>"
                + "<tr><td><strong>Type</strong></td>"
                + "<td><span editable-select='informationDashboard.type.value' e-form='infoeditform' e-ng-options='key for (key, value) in ipselecttemplate.type'>{{informationDashboard.type.value}}</span></td></tr>"
                + "<tr><td><strong>DFT</strong></td>"
                + "<td><span editable-select='informationDashboard.dft.value' e-form='infoeditform' e-ng-options='key for (key, value) in ipselecttemplate.dft'>{{informationDashboard.dft.value}}</span></td></tr>"
                + "<tr><td><strong>Chip + (#instances)</strong></td>"
                + "<td><span ng-bind-html='trustAsHtml(informationDashboard.chip_instance)'></span></td></tr>"
                + "<tr ng-repeat='info in informationDashboard.data | orderBy:[infopredicate1, infopredicate2] | filter:filterInfo'>"
                + "<td><strong editable-text='info.key' e-form='infoeditform' edit-disabled='!info.isNew'>{{ info.key }}</strong></td>"
                + "<td><span editable-text='info.value' e-form='infoeditform' edit-disabled='!info.editable'>{{ info.value }}</span></td>"
                + "</tr>"
                + "</tbody></table></form>"
            }
        });

App
    .directive(
        'ipInformationRevChangeEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='infoRevChangeEditform' onaftersave='saveInfoRevChange()' oncancel='cancel()'>"
                + "<div class='btn-form' ng-show='infoRevChangeEditform.$visible'>"
                + "<button type='submit' ng-disabled='infoRevChangeEditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a ng-disabled='infoRevChangeEditform.$waiting' ng-click='infoRevChangeEditform.$cancel(); cancelSaveInfoDashboard();' class='btn btn-xs pull-right'>Cancel</a></div>"
                + "<table class='table table-condensed table-bordered' >"
                + "<tbody>"
                + "<tr ng-repeat='info in informationDashboard.revChange | orderBy:[infopredicate1, infopredicate2] | filter:filterInfo'>"
                + "<td><strong editable-text='info.key' e-form='infoRevChangeEditform' edit-disabled='!info.isNew'>{{ info.key }}</strong></td>"
                + "<td><span editable-text='info.value' e-form='infoRevChangeEditform' edit-disabled='!info.editable'>{{ info.value }}</span></td>"
                + "</tr>"
                + "</tbody></table></form>"
            }
        });

App
    .directive(
        'informationTableEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='infoeditform' onaftersave='saveInfoTable()' oncancel='cancel()'>"
                + "<div class='btn-form' ng-show='infoeditform.$visible'>"
                + "<button type='submit' ng-disabled='infoeditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a ng-disabled='infoeditform.$waiting' ng-click='infoeditform.$cancel(); cancelSaveInfoTable()' class='btn btn-xs pull-right'>Cancel</a></div>"
                + "<table class='table table-condensed table-bordered table-header-bg'>"
                + "<thead><tr><th style='width:15%;'>FIELD</th>"
                + "<th style='width:15%;border:3px solid #F5f6f7 !important;' ng-repeat='(infoHeader, value) in informationTable.title track by $index' ng-if='value || infoeditform.$visible'>{{infoHeader}}</th>"
                + "</tr></thead>"
                + "<tbody>"
                + "<tr ng-repeat='(name, arr) in informationTable.data'>"
                + "<td style='border:3px solid #F5f6f7 !important;'><b>{{name}}</b></td>"
                + "<td style='border:3px solid #F5f6f7 !important;' ng-repeat='(header, v) in informationTable.title track by $index' ng-if='v || infoeditform.$visible' ng-class={'lighgrey':!arr[header].editable}>"
                + "<span editable-text='arr[header].value' e-form='infoeditform' edit-disabled='!arr[header].editable'>{{arr[header].value}}</span>"
                + "</td>"
                + "</tr>"
                + "</tbody>	</table></form>"
            }
        });

App
    .directive(
        'contactEditForm',
        function () {
            return {
                // e-typeahead-on-select='selectUser($item, $model,
                // contact)'
                restrict: 'ACE',
                template: "<form editable-form name='contacteditform' onaftersave='saveContactTable()' oncancel='cancel()'>"
                + "<div class='btn-form' ng-show='contacteditform.$visible'>"
                + "<a ng-disabled='contacteditform.$waiting' ng-click='addNewContact(contacts)' class='btn btn-xs pull-right'>New</a>"
                + "<button type='submit' ng-disabled='contacteditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a ng-disabled='contacteditform.$waiting' ng-click='contacteditform.$cancel(); cancelSaveContact()' class='btn btn-xs pull-right'>Cancel</button></div>"
                + "<table class='table table-condensed table-bordered' >"
                + "<tbody><tr ng-repeat='contact in contacts | filter:filterContact'><td style='word-wrap:break-word;'>"
                + "<strong editable-text='contact.key' e-form='contacteditform' e-required>{{ contact.key | myUpperCase }}</strong></td>"
                + "<td style='word-wrap:break-word;'>"
                + "<span editable-text='contact.value' e-form='contacteditform'>{{ contact.value}}</span>"
                + "</td>"
                + "<td  ng-show='contacteditform.$visible'>"
                + "<a ng-click='deleteContact(contact)' class='btn btn-xs pull-right'><strong class='text-danger'>Delete</strong></a></td></tr>"
                + "</tbody></table></form>"
            }
        });

App
    .directive(
        'ipChipTableEditForm',
        function () {
            return {
                // e-typeahead-on-select='selectUser($item, $model, contact)'
                restrict: 'ACE',
                template: "<form editable-form name='ipchipeditform' onaftersave='saveIPChipTable()' oncancel='cancel()'>"
                + "<div class='btn-form' ng-show='ipchipeditform.$visible'>"
                + "<a ng-disabled='ipchipeditform.$waiting' ng-click='addNewIPChipTable(ipChipTable)' class='btn btn-xs pull-right'>New</a>"
                + "<button type='submit' ng-disabled='ipchipeditform.$waiting || !ipchipeditform.$valid' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a ng-disabled='ipchipeditform.$waiting' ng-click='ipchipeditform.$cancel(); cancelSaveIPChipTable()' class='btn btn-xs pull-right'>Cancel</button></div>"
                + "<table class='table table-condensed table-bordered' >"
                + "<tbody>"
                + "<tr ng-repeat='ip in ipChipTable'>"
                + "<td data-ng-hide='ipchipeditform.$visible' ng-class='ip.schedule_flag'></td>"
                + "<td style='word-wrap:break-word;'>"
                //	+ "<strong editable-text='contact.key' e-form='ipchipeditform'>{{ ip.key | myUpperCase }}</strong></td>"
                + "<a ng-href='{{ip.url}}' data-ng-hide='ipchipeditform.$visible'>{{ip.displayName}}</a>"
                + "<md-autocomplete flex required ng-show='ipchipeditform.$visible' md-input-name='displayName' md-input-minlength='2' md-input-maxlength='64' md-delay='10'"
                + "md-selected-item='selectedIP' md-search-text='ip.displayName' md-item-text='item.pname'"
                + "md-selected-item-change='selectIP(item, ip)' md-items='item in searchIPProgram(ip.displayName)'>"
                + "<span md-highlight-text='ip.displayName'> {{item.pname + ' '+ item.rname}} </span>"
                + "</md-autocomplete> "
                + "<td style='word-wrap:break-word;'>"
                + "<span ng-hide='ipchipeditform.$visible'>{{ip.instances}}</span>"
                + "<md-input-container class='md-block' ng-show='ipchipeditform.$visible'>"
                + "<input required type='number' name='instances' ng-model='ip.instances' min='0' max='100' aria-label='instances'/>"
                + "<div ng-messages='ipchipeditform.instances.$error' multiple >"
                + "<div ng-message='required'>Required field</div>"
                + "<div ng-message='min'>Should be greater than 0</div>"
                + "</div></md-input-container>"
                + "</td>"
                + "<td ng-show='ipchipeditform.$visible'>"
                + "<a ng-click='deleteIPChipTable(ip)' ng-disabled='ipchipeditform.$waiting || !ipchipeditform.$valid' class='btn btn-xs pull-right'><strong class='text-danger'>Delete</strong></a></td></tr>"
                + "</tbody></table></form>"
            }
        });

App
    .directive(
        'remarkEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='remarkeditform' onaftersave='saveRemark();' oncancel='cancel();remark.tinymce=remark.content;' ng-show='remarkeditform.$visible'>"
                + "<textarea ui-tinymce='tinymceOptions' ng-model='remark.tinymce'></textarea>"
                + "</form>"
            }
        });

App
    .directive(
        'ipCatEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='ipcateditform' onaftersave='saveIPCatHeadline();' oncancel='cancel();ipcat.tinymce=ipcat.content;' ng-show='ipcateditform.$visible'>"
                + "<textarea ui-tinymce='tinymceOptions' ng-model='ipcat.tinymce'></textarea>"
                + "</form>"
            }
        });
App
    .directive(
        "internalMilestoneTable",
        function () {
            return {
                restrict: 'ACE',
                template: "<table class='table table-bordered table-condensed table-header-bg' ng-hide='milestoneeditform.$visible' id='milestone-table'>"
                + "<thead><tr><th class='theader' style='width:2%;'>S</th>"
                + "<th class='theader' style='width:13%;'>Milestone</th>"
                + "<th class='theader' style='width:10%;'>Plan Start</th>"
                + "<th class='theader' style='width:10%;'>Actual Start</th>"
                + "<th class='theader' style='width:10%;'>Plan End</th>"
                + "<th class='theader' style='width:10%;'>Actual End</th>"
                + "<th class='theader noexport' style='width:45%;'>Note</th></tr></thead>"
                + "<tbody>"
                + "<tr ng-repeat='milestone in milestones track by $index' ng-click='milestoneRowClick($index, $event,milestone);' >"
                + "<td ng-class='(milestone.tstatus|lowercase)'></td>"
                + "<td id='milestone.id'>"
                + "<strong>{{milestone.tname|myUpperCase}}</strong>"
                + "</td>"
                + "<td class='text-center' ng-class='{hasComment: milestone.plan_start.hasComment}'>"
                + "<p title = '{{milestone.plan_start.MILESTONE[0].comment}}' ng-bind=milestone.plan_start.MILESTONE[0].value ng-class=milestone.plan_start.MILESTONE[0].dhstatus.toLowerCase()></p>"
                + "<p ng-show='displayMilestoneStrikeout' ng-if='!$first' ng-repeat='ps in milestone.plan_start.MILESTONE track by $index' title = '{{ps.comment}}' class='text-grey text-history noexport' ng-bind=ps.value></p>"
                + "</td>"
                + "<td class='text-center' ng-class='{hasComment: milestone.actual_start.hasComment}'>"
                + "<p title = '{{milestone.actual_start.MILESTONE[0].comment}}' ng-bind=milestone.actual_start.MILESTONE[0].value ng-class=milestone.actual_start.MILESTONE[0].dhstatus.toLowerCase()></p>"
                + "<p ng-show='displayMilestoneStrikeout' ng-if='!$first' ng-repeat='as in milestone.actual_start.MILESTONE track by $index' title = '{{as.comment}}' class='text-grey text-history noexport' ng-bind=as.value></p>"
                + "</td>"
                + "<td class='text-center' ng-class='{hasComment: milestone.current_end.hasComment}'>"
                + "<p title = '{{milestone.current_end.MILESTONE[0].comment}}'  ng-bind=milestone.current_end.MILESTONE[0].value ng-class=milestone.current_end.MILESTONE[0].dhstatus></p>"
                + "<p ng-show='displayMilestoneStrikeout' ng-if='!$first' ng-repeat='ce in milestone.current_end.MILESTONE track by $index' title = '{{ce.comment}}' class='text-grey text-history noexport' ng-bind=ce.value></p>"
                + "</td>"
                + "<td class='text-center' ng-class='{hasComment: milestone.actual_end.hasComment}'>"
                + "<p title = '{{milestone.actual_end.MILESTONE[0].comment}}'  ng-bind=milestone.actual_end.MILESTONE[0].value ng-class=milestone.actual_end.MILESTONE[0].dhstatus></p>"
                + "<p ng-show='displayMilestoneStrikeout' ng-if='!$first' ng-repeat='ae in milestone.actual_end.MILESTONE track by $index' title = '{{ae.comment}}' class='text-grey text-history noexport' ng-bind=ae.value></p>"
                + "</td>"
                + "<td ng-bind-html='trustAsHtml(milestone.tnote)' class='noexport'></td>"
                + "</td>" + "</tr>" + "</tbody>" + "</table>"
            }
        });

App
    .directive(
        "customerMilestoneTable",
        function () {
            return {
                restrict: 'ACE',
                template: "<table class='table table-bordered table-condensed table-header-bg' ng-hide='milestoneeditform.$visible' id='milestone-table'>"
                + "<thead><tr><th class='theader' style='width:2%;'>S</th>"
                + "<th class='theader' style='width:18%;'>Milestone</th>"
                + "<th class='theader' style='width:15%;'>Plan</th>"
                + "<th class='theader' style='width:15%;'>Actual</th>"
                + "<th class='theader noexport' style='width:50%;'>Note</th></tr></thead>"
                + "<tbody >"
                + "<tr ng-repeat='milestone in milestones track by $index' ng-click='milestoneRowClick($index, $event,milestone);' >"
                + "<td ng-class='(milestone.tstatus|lowercase)'></td>"
                + "<td id='milestone.id'>"
                + "<strong>{{milestone.tname|myUpperCase}}</strong>"
                + "</td>"
                + "<td class='text-center' ng-class='{hasComment: milestone.current_end.hasComment}'>"
                + "<p title = '{{milestone.current_end.MILESTONE[0].comment}}'  ng-bind=milestone.current_end.MILESTONE[0].value ng-class=milestone.current_end.MILESTONE[0].dhstatus></p>"
                + "<p ng-show='displayMilestoneStrikeout' ng-if='!$first' ng-repeat='ce in milestone.current_end.MILESTONE track by $index' title = '{{ce.comment}}' class='text-grey text-history noexport' ng-bind=ce.value></p>"
                + "</td>"
                + "<td class='text-center' ng-class='{hasComment: milestone.actual_end.hasComment}'>"
                + "<p title = '{{milestone.actual_end.MILESTONE[0].comment}}'  ng-bind=milestone.actual_end.MILESTONE[0].value ng-class=milestone.actual_end.MILESTONE[0].dhstatus></p>"
                + "<p ng-show='displayMilestoneStrikeout' ng-if='!$first' ng-repeat='ae in milestone.actual_end.MILESTONE track by $index' title = '{{ae.comment}}' class='text-grey text-history noexport' ng-bind=ae.value></p>"
                + "</td>"
                + "<td ng-bind-html='trustAsHtml(milestone.tnote)' class='noexport'></td>"
                + "</td>" + "</tr>" + "</tbody>" + "</table>"
            }
        });
App
    .directive(
        "ipMilestoneTable",
        function () {
            return {
                restrict: 'ACE',
                template: "<table class='table table-bordered table-condensed table-header-bg' ng-hide='milestoneeditform.$visible' id='milestone-table'>"
                + "<thead><tr><th class='theader' style='width:2%;'>S</th>"
                + "<th class='theader' style='width:13%;'>Milestone</th>"
                + "<th class='theader' style='width:10%;'>Plan</th>"
                + "<th class='theader' style='width:10%;'>Current</th>"
                + "<th class='theader' style='width:10%;'>Actual</th>"
                + "<th class='theader noexport' style='width:55%;'>Note</th></tr></thead>"
                + "<tbody>"
                + "<tr ng-repeat='milestone in milestones track by $index' ng-click='milestoneRowClick($index, $event,milestone);' >"
                + "<td ng-class='(milestone.tstatus|lowercase)'></td>"
                + "<td id='milestone.id'>"
                + "<strong>{{milestone.tname|myUpperCase}}</strong>"
                + "</td>"
                + "<td class='text-center' ng-class='{hasComment: milestone.actual_start.hasComment}'>"
                + "<p title = '{{milestone.actual_start.MILESTONE[0].comment}}' ng-bind=milestone.actual_start.MILESTONE[0].value ng-class=milestone.actual_start.MILESTONE[0].dhstatus.toLowerCase()></p>"
                + "<p ng-show='displayMilestoneStrikeout' ng-if='!$first' ng-repeat='as in milestone.actual_start.MILESTONE track by $index' title = '{{as.comment}}' class='text-grey text-history noexport' ng-bind=as.value></p>"
                + "</td>"
                + "<td class='text-center' ng-class='{hasComment: milestone.current_end.hasComment}'>"
                + "<p title = '{{milestone.current_end.MILESTONE[0].comment}}'  ng-bind=milestone.current_end.MILESTONE[0].value ng-class=milestone.current_end.MILESTONE[0].dhstatus></p>"
                + "<p ng-show='displayMilestoneStrikeout' ng-if='!$first' ng-repeat='ce in milestone.current_end.MILESTONE track by $index' title = '{{ce.comment}}' class='text-grey text-history noexport' ng-bind=ce.value></p>"
                + "</td>"
                + "<td class='text-center' ng-class='{hasComment: milestone.actual_end.hasComment}'>"
                + "<p title = '{{milestone.actual_end.MILESTONE[0].comment}}'  ng-bind=milestone.actual_end.MILESTONE[0].value ng-class=milestone.actual_end.MILESTONE[0].dhstatus></p>"
                + "<p ng-show='displayMilestoneStrikeout' ng-if='!$first' ng-repeat='ae in milestone.actual_end.MILESTONE track by $index' title = '{{ae.comment}}' class='text-grey text-history noexport' ng-bind=ae.value></p>"
                + "</td>"
                + "<td ng-bind-html='trustAsHtml(milestone.tnote)' class='noexport'></td>"
                + "</td>" + "</tr>" + "</tbody>" + "</table>"
            }
        });

App
    .directive(
        'internalMilestoneEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='milestoneeditform' onaftersave='saveMilestone()' oncancel='cancel()' ng-show='milestoneeditform.$visible'>"
                + "<div layout-gt-xs='row'>"
                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<button type='submit' ng-disabled='milestoneeditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a type='button' ng-disabled='milestoneeditform.$waiting' ng-click='resetMilestoneSelect();milestoneeditform.$cancel();' class='btn btn-xs pull-right'>Cancel</a>"
                + "</md-input-container>"
                + "</div>"

                + "<div layout-gt-xs='row'>"
                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<label>Milestone Name</label>"
                + "<input ng-model='milestoneSelected.name' name='milestoneName' class='form-control' ng-disabled='ms.currentCategory.toLowerCase()==projectCatName' required" +
                " ng-minlength='2' ng-maxlength='256' />"
                + "<div ng-messages='milestoneeditform.milestoneSelected.name.$error'>"
                + "<div ng-message='required'>Milestone name is required.</div>"
                + "<div ng-message='minlength'>Milestone name is too short.</div>"
                + "<div ng-message='maxlength'>Milestone name is too long(256 char max).</div>"
                + "</div>"
                + "</md-input-container>"

                + "<md-input-container>"
                + "<label>Order Number</label>"
                + "<input class='form-control' type='number' ng-model='milestoneSelected.order' name='milestoneOrder' ng-disabled='ms.currentCategory.toLowerCase()==projectCatName' " +
                "required type='number' min='0' max='10000'>"
                + "<div ng-messages='milestoneeditform.milestoneSelected.order.$error' md-auto-hide='false'>"
                + "<div ng-message='required'>Milestone Order is required!</div>"
                + "<div ng-message='min'>Must be greater than 0</div>"
                + "<div ng-message='max'>Out of range</div>"
                + "</div>"
                + "</md-input-container>"
                + "</div>"

                + "<div layout-gt-xs='row'>"
                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<label>Plan Start</label>"
                + "<span layout='column'>"
                + "<md-radio-group ng-model='milestoneSelected.planStart.optionModel' layout='row'>"
                + "<md-radio-button value='NA'>NA</md-radio-button>"
                + "<md-radio-button value='TBD'>TBD</md-radio-button>"
                + "<md-radio-button value='DATE'>DATE</md-radio-button>"
                + "</md-radio-group>"
                + "</span>"
                + "<span layout='column' >"
                + "<md-datepicker flex  ng-focus=milestoneSelected.planStart.optionModel='DATE' ng-model='milestoneSelected.planStart.date' md-placeholder='Plan Start Date'></md-datepicker>"
                + "</span>"
                + "<span layout='column'>"
                + "<textarea ng-model='milestoneSelected.planStart.comment' maxlength='256' rows='2' md-select-on-focus placeholder='comment (256 char max)'></textarea>"
                + "</span>"
                + "</md-input-container>"

                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<label>Actual Start</label>"
                + "<span layout='column'>"
                + "<md-radio-group ng-model='milestoneSelected.actualStart.optionModel' layout='row'>"
                + "<md-radio-button value='NA'>NA</md-radio-button>"
                + "<md-radio-button value='TBD'>TBD</md-radio-button>"
                + "<md-radio-button value='DATE'>DATE</md-radio-button>"
                + "</md-radio-group>"
                + "</span>"
                + "<span layout='column'>"
                + "<md-datepicker flex ng-focus=milestoneSelected.actualStart.optionModel='DATE' ng-model='milestoneSelected.actualStart.date' md-placeholder='Actual Start Date'></md-datepicker>"
                + "</span>"
                + "<span layout='column'>"
                + "<textarea ng-model='milestoneSelected.actualStart.comment' maxlength='256' rows='2' md-select-on-focus placeholder='comment (256 char max)'></textarea>"
                + "</span>"
                + "</md-input-container>"

                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<label> Plan End : {{milestoneSelected.currentEnd.snapshot}}</label>"
                + "<span layout='column'>"
                + "<md-radio-group ng-model='milestoneSelected.currentEnd.optionModel'  layout='row'>"
                + "<md-radio-button value='NA'>NA</md-radio-button>"
                + "<md-radio-button value='TBD'>TBD</md-radio-button>"
                + "<md-radio-button value='DATE'>DATE</md-radio-button>"
                + "</md-radio-group>"
                + "</span>"
                + "<span layout='column'>"
                + "<md-datepicker flex  ng-focus=milestoneSelected.currentEnd.optionModel='DATE' ng-model='milestoneSelected.currentEnd.date' md-placeholder='Plan End Date'></md-datepicker>"
                + "</span>"
                + "<span layout='column' ng-hide='!allowOrange'> "
                + "<md-checkbox ng-model='ms.orangeStatus'>"
                + "<span class='text-orange'>Orange Override</span>"
                + "</md-checkbox>"
                + "</span>"
                + "<span layout='column'>"
                + "<textarea ng-model='milestoneSelected.currentEnd.comment' maxlength='256' rows='2' md-select-on-focus placeholder='comment (256 char max)'></textarea>"
                + "</span>"
                + "</md-input-container>"

                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<label>Actual End</label>"
                + "<span layout='column'>"
                + "<md-radio-group layout='row' ng-model='milestoneSelected.actualEnd.optionModel' >"
                + "<md-radio-button value='NA'>NA</md-radio-button>"
                + "<md-radio-button value='DATE'>DATE</md-radio-button>"
                + "</md-radio-group>"
                + "</span>"
                + "<span layout='column'>"
                + "<md-datepicker flex ng-focus=milestoneSelected.actualEnd.optionModel='DATE' ng-model='milestoneSelected.actualEnd.date' md-placeholder='Actual End Date'></md-datepicker>"
                + "</span>"
                + "<span layout='column'>"
                + "<textarea ng-model='milestoneSelected.actualEnd.comment' maxlength='256' rows='2' md-select-on-focus placeholder='comment (256 char max)'></textarea>"
                + "</span>"
                + "</md-input-container>"

                + "</div>"

                + "<div layout-gt-xs='row'>"
                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<label>Note</label> "
                + "<span layout='column'>"
                + "<textarea flex ng-model='milestoneSelected.note' maxlength='512' rows='5' md-select-on-focus placeholder='Note (512 char max)'></textarea>"
                + "</span>"
                + "</md-input-container>"
                + "</div>"

                + "</form>"
            }
        });

App
    .directive(
        'customerMilestoneEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='milestoneeditform' onaftersave='saveMilestone()' oncancel='cancel()' ng-show='milestoneeditform.$visible'>"
                + "<div layout-gt-xs='row'>"
                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<button type='submit' ng-disabled='milestoneeditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a type='button' ng-disabled='milestoneeditform.$waiting' ng-click='resetMilestoneSelect();milestoneeditform.$cancel();' class='btn btn-xs pull-right'>Cancel</a>"
                + "</md-input-container>"
                + "</div>"

                + "<div layout-gt-md='row'>"
                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<label>Milestone Name</label>"
                + "<input ng-model='milestoneSelected.name' name='milestoneName' class='form-control' ng-disabled='ms.currentCategory.toLowerCase()==projectCatName' required" +
                " ng-minlength='2' ng-maxlength='256'>"
                + "<div ng-messages='milestoneeditform.milestoneSelected.name.$error'>"
                + "<div ng-message='required'>Milestone name is required.</div>"
                + "<div ng-message='minlength'>Milestone name is too short.</div>"
                + "<div ng-message='maxlength'>Milestone name is too long(256 char max).</div>"
                + "</div>"
                + "</md-input-container>"

                + "<md-input-container>"
                + "<label>Order Number</label>"
                + "<input class='form-control' type='number' ng-model='milestoneSelected.order' name='milestoneOrder' ng-disabled='ms.currentCategory.toLowerCase()==projectCatName' " +
                "required type='number' min='1' max='10000'>"
                + "<div ng-messages='milestoneeditform.milestoneSelected.order.$error'>"
                + "<div ng-message='required'>Milestone Order is required!</div>"
                + "<div ng-message='min'>Must be greater than 0</div>"
                + "<div ng-message='max'>Out of range</div>"
                + "</div>"
                + "</md-input-container>"
                + "</div>"

                + "<div layout-gt-md='row'>"

                + "<div class='md-block' flex-gt-sm>"
                + "<label>Plan</label>"
                + "<span layout='column'>"
                + "<md-radio-group ng-model='milestoneSelected.currentEnd.optionModel'  layout='row'>"
                + "<md-radio-button value='NA'>NA</md-radio-button>"
                + "<md-radio-button value='TBD'>TBD</md-radio-button>"
                + "<md-radio-button value='DATE'>DATE</md-radio-button>"
                + "</md-radio-group>"
                + "</span>"
                + "<span layout='column'>"
                + "<md-datepicker flex  ng-focus=milestoneSelected.currentEnd.optionModel='DATE' ng-model='milestoneSelected.currentEnd.date' md-placeholder='Plan Date'></md-datepicker>"
                + "</span>"
                + "<span layout='column' ng-hide='!allowOrange'> "
                + "<md-checkbox ng-model='ms.orangeStatus'>"
                + "<span class='text-orange'>Orange Override</span>"
                + "</md-checkbox>"
                + "</span>"
                + "<span layout='column'>"
                + "<textarea ng-model='milestoneSelected.currentEnd.comment' maxlength='256' rows='3' md-select-on-focus placeholder='comment (256 char max)'></textarea>"
                + "</span>"
                + "</div>"

                + "<div class='md-block' flex-gt-sm>"
                + "<label>Actual </label>"
                + "<span layout='column'>"
                + "<md-radio-group layout='row' ng-model='milestoneSelected.actualEnd.optionModel' >"
                + "<md-radio-button value='NA'>NA</md-radio-button>"
                + "<md-radio-button value='DATE'>DATE</md-radio-button>"
                + "</md-radio-group>"
                + "</span>"
                + "<span layout='column'>"
                + "<md-datepicker flex ng-focus=milestoneSelected.actualEnd.optionModel='DATE' ng-model='milestoneSelected.actualEnd.date' md-placeholder='Actual Date'></md-datepicker>"
                + "</span>"
                + "<span layout='column'>"
                + "<textarea ng-model='milestoneSelected.actualEnd.comment' maxlength='256' rows='3' md-select-on-focus placeholder='comment (256 char max)'></textarea>"
                + "</span>"
                + "</div>"

                + "</div>"

                + "<div layout-gt-xs='row'>"
                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<label>Note</label> "
                + "<span layout='column'>"
                + "<textarea flex ng-model='milestoneSelected.note' maxlength='512' rows='5' md-select-on-focus placeholder='Note (512 char max)'></textarea>"
                + "</span>"
                + "</md-input-container>"
                + "</div>"

                + "</form>"
            }
        });

App
    .directive(
        'ipMilestoneEditForm',
        function () {
            return {
                restrict: 'ACE',
                template: "<form editable-form name='milestoneeditform' onaftersave='saveMilestone()' oncancel='cancel()' ng-show='milestoneeditform.$visible'>"
                + "<div layout-gt-xs='row'>"
                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<button type='submit' ng-disabled='milestoneeditform.$waiting' class='btn btn-xs btn-link pull-right'>Save</button>"
                + "<a type='button' ng-disabled='milestoneeditform.$waiting' ng-click='resetMilestoneSelect();milestoneeditform.$cancel();' class='btn btn-xs pull-right'>Cancel</a>"
                + "</md-input-container>"
                + "</div>"

                + "<div layout-gt-xs='row'>"
                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<label>Milestone Name</label>"
                + "<input ng-model='milestoneSelected.name' name='milestoneName' class='form-control' required" +
                " ng-minlength='2' ng-maxlength='256' />"
                + "<div ng-messages='milestoneeditform.milestoneSelected.name.$error'>"
                + "<div ng-message='required'>Milestone name is required.</div>"
                + "<div ng-message='minlength'>Milestone name is too short.</div>"
                + "<div ng-message='maxlength'>Milestone name is too long(256 char max).</div>"
                + "</div>"
                + "</md-input-container>"

                + "<md-input-container>"
                + "<label>Order Number</label>"
                + "<input class='form-control' type='number' ng-model='milestoneSelected.order' name='milestoneOrder' " +
                "required type='number' min='0' max='10000'>"
                + "<div ng-messages='milestoneeditform.milestoneSelected.order.$error' md-auto-hide='false'>"
                + "<div ng-message='required'>Milestone Order is required!</div>"
                + "<div ng-message='min'>Must be greater than 0</div>"
                + "<div ng-message='max'>Out of range</div>"
                + "</div>"
                + "</md-input-container>"
                + "</div>"

                + "<div layout-gt-xs='row'>"

                + "<div class='md-block' flex-gt-xs>"
                + "<label>Plan</label>"
                + "<span layout='column'>"
                + "<md-radio-group ng-model='milestoneSelected.actualStart.optionModel' layout='row'>"
                + "<md-radio-button value='NA'>NA</md-radio-button>"
                + "<md-radio-button value='TBD'>TBD</md-radio-button>"
                + "<md-radio-button value='DATE'>DATE</md-radio-button>"
                + "</md-radio-group>"
                + "</span>"
                + "<span layout='column'>"
                + "<md-datepicker flex ng-focus=milestoneSelected.actualStart.optionModel='DATE' ng-model='milestoneSelected.actualStart.date' md-placeholder='Plan Date'></md-datepicker>"
                + "</span>"
                + "<span layout='column'>"
                + "<textarea ng-model='milestoneSelected.actualStart.comment' maxlength='256' rows='2' md-select-on-focus placeholder='comment (256 char max)'></textarea>"
                + "</span>"
                + "</div>"


                + "<div class='md-block' flex-gt-xs>"
                + "<label> Current : {{milestoneSelected.currentEnd.snapshot}}</label>"
                + "<span layout='column'>"
                + "<md-radio-group ng-model='milestoneSelected.currentEnd.optionModel'  layout='row'>"
                + "<md-radio-button value='NA'>NA</md-radio-button>"
                + "<md-radio-button value='TBD'>TBD</md-radio-button>"
                + "<md-radio-button value='DATE'>DATE</md-radio-button>"
                + "</md-radio-group>"
                + "</span>"
                + "<span layout='column'>"
                + "<md-datepicker flex  ng-focus=milestoneSelected.currentEnd.optionModel='DATE' ng-model='milestoneSelected.currentEnd.date' md-placeholder='Current Date'></md-datepicker>"
                + "</span>"
                + "<span layout='column' ng-hide='!allowOrange'> "
                + "<md-checkbox ng-model='ms.orangeStatus'>"
                + "<span class='text-orange'>Orange Override</span>"
                + "</md-checkbox>"
                + "</span>"
                + "<span layout='column'>"
                + "<textarea ng-model='milestoneSelected.currentEnd.comment' maxlength='256' rows='2' md-select-on-focus placeholder='comment (256 char max)'></textarea>"
                + "</span>"
                + "</div>"


                + "<div class='md-block' flex-gt-xs>"
                + "<label>Actual</label>"
                + "<span layout='column'>"
                + "<md-radio-group layout='row' ng-model='milestoneSelected.actualEnd.optionModel' >"
                + "<md-radio-button value='NA'>NA</md-radio-button>"
                + "<md-radio-button value='DATE'>DATE</md-radio-button>"
                + "</md-radio-group>"
                + "</span>"
                + "<span layout='column'>"
                + "<md-datepicker flex ng-focus=milestoneSelected.actualEnd.optionModel='DATE' ng-model='milestoneSelected.actualEnd.date' md-placeholder='Actual Date'></md-datepicker>"
                + "</span>"
                + "<span layout='column'>"
                + "<textarea ng-model='milestoneSelected.actualEnd.comment' maxlength='256' rows='2' md-select-on-focus placeholder='comment (256 char max)'></textarea>"
                + "</span>"
                + "</div>"

                + "</div>"

                + "<div layout-gt-xs='row'>"
                + "<md-input-container class='md-block' flex-gt-xs>"
                + "<label>Note</label> "
                + "<span layout='column'>"
                + "<textarea flex ng-model='milestoneSelected.note' maxlength='512' rows='5' md-select-on-focus placeholder='Note (512 char max)'></textarea>"
                + "</span>"
                + "</md-input-container>"
                + "</div>"

                + "</form>"
            }
        });

App.directive('ckEditor', function () {
    return {
        require: '?ngModel',
        link: function (scope, elm, attr, ngModel) {
            var ck = CKEDITOR.replace(elm[0]);

            if (!ngModel)
                return;

            ck.on('pasteState', function () {
                scope.$apply(function () {
                    ngModel.$setViewValue(ck.getData());
                });
            });

            ngModel.$render = function (value) {
                ck.setData(ngModel.$viewValue);
            };
        }
    };
});