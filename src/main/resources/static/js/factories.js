/**
 *
 */

App.factory("authenticationFactory", function ($http, $rootScope, $filter, $q, Notification) {
    var authObj = {
        check: function () {
            $rootScope.authenticated = false;
            $http.get("/api/auth/user", {ignoreLoadingBar: true})
                .then(function (ret) {
                    $rootScope.authenticated = ret.authenticated;
                    if (ret.principal && ret.principal.authorities) {
                        angular.forEach(ret.principal.authorities, function (arr, key) {
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
                });
        }
    };

    return authObj;
});

App.factory("contactFactory", function ($http, $filter, $q, Notification) {
    var contactObj = {
        get: function (rid, reload) {
            return $http.get('/api/revision/getContact?rid=' + rid + "&reload=" + reload);
        },
        filter: function (contact) {
            return contact.isDeleted !== true;
        },
        cancel: function (contacts) {
            for (var i = contacts.length; i--;) {
                var contact = contacts[i];
                if (contact.isDeleted) {
                    delete contact.isDeleted;
                }
                if (contact.isNew) {
                    contacts.splice(i, 1);
                }
            }
            return contacts;
        },
        add: function (contacts) {
            return contacts.push({
                id: -1,
                key: '',
                value: '',
                isNew: true
            });
        },
        save: function (rid, contacts, callback) {
            var obj = {};
            obj.rid = rid;
            obj.data = contacts;
            $http.post('/api/revision/saveContact', obj)
                .then(function (result) {
                    Notification.then({message: result.data, delay: 1000});
                    callback();
                }, function (ret) {
                Notification.error({message: ret.data, delay: 5000, title: ret.code});
            });
        },
        del: function (contacts, id) {
            var filtered = $filter('filter')(contacts, {id: id});
            if (filtered.length) {
                filtered[0].isDeleted = true;
            }
            return contacts;
        }
    };
    return contactObj;
});
App.factory("infoFactory", function ($http, $filter, $q, Notification) {
    var infoObject = {
        getInfoDashboard: function (rid, reload, callback) {
            var ret = [];
            $http.get('/api/revision/getInformation?rid=' + rid + "&type=dashboard&reload=" + reload)
                .then(function (result) {
                    callback(result);
                }, function (data, code) {
                    Notification.error(data);
                });
        },
        getInfoTable: function (rid, reload) {
            return $http.get('/api/revision/getInformation?rid=' + rid + "&type=detail&reload=" + reload)
        },
        filter: function (info) {
            return info.isDeleted !== true;
        },
        cancel: function (infos) {
            for (var i = infos.length; i--;) {
                var info = infos[i];
                if (info.isDeleted) {
                    delete info.isDeleted;
                }
                if (info.isNew) {
                    infos.splice(i, 1);
                }
            }
            return infos;
        },
        cancelInfoDashboard: function (infos) {
            for (var i = infos.data.length; i--;) {
                var info = infos.data[i];
                if (info.isDeleted) {
                    delete info.isDeleted;
                }
                if (info.isNew) {
                    infos.data.splice(i, 1);
                }
            }
            return infos;
        },
        addInfoDasboard: function (infos) {
            return infos.data.push({
                id: 0,
                key: '',
                order: infos.data.length + 10,
                value: '',
                editable: true,
                isNew: true
            });
        },
        addInfoTable: function (infos) {
            var body = {};
            body.field = "";
            body.level = 1;
            body.isNew = true;
            body.data = [];
            angular.forEach(infos.header, function (key, index) {
                if (!key.match(/field/i)) {
                    var obj = {};
                    obj.field = key;
                    obj.id = 0;
                    obj.name = "";
                    obj.value = "";
                    body.data.push(obj);
                }
            });
            return infos.body.push(body);
        },
        saveInfoTable: function (rid, infos, callback) {
            infos.rid = rid;
            $http.post('/api/revision/saveInformation', infos)
                .then(function (result) {
                    Notification.then({message: result.data, delay: 1000});
                    callback();
                }, function (ret) {
                Notification.error({message: ret.data, delay: 5000, title: ret.code});
            });
        },
        del: function (infos, info) {
            var filtered = $filter('filter')(infos.body, {field: info.field});
            if (filtered.length) {
                filtered[0].isDeleted = true;
            }
            return infos;
        }
    };
    return infoObject;
});

App.factory("linkFactory", function ($http, $filter, $q, Notification) {
    var linkObject = {
        get: function (rid, reload) {
            return $http.get('/api/revision/getMeetingLink?rid=' + rid + "&reload=" + reload);
        },
        filter: function (meeting) {
            return meeting.isDeleted !== true;
        },
        add: function (meetings) {
            return meetings.push({
                id: meetings.length + 1,
                key: '',
                name: null,
                url: '',
                isNew: true
            });
        },
        del: function (meetings, id) {
            var filtered = $filter('filter')(meetings, {id: id});
            if (filtered.length) {
                filtered[0].isDeleted = true;
            }
            return meetings;
        },
        cancel: function (meetings) {
            for (var i = meetings.length; i--;) {
                var meeting = meetings[i];
                // undelete
                if (meeting.isDeleted) {
                    delete meeting.isDeleted;
                }
                // remove new
                if (meeting.isNew) {
                    meetings.splice(i, 1);
                }
            }
            return meetings;
        },
        save: function (rid, meetings, type, callback) {
            var obj = {};
            obj.rid = rid;
            obj.data = meetings;
            obj.type = type;
            $http.post('/api/revision/saveMeetingLink', obj)
                .then(function (result) {
                    Notification.then({message: result.data, delay: 1000});
                    callback();
                },function (ret) {
                Notification.error({message: ret.data, delay: 5000, title: ret.code});
            });

//			var promises = [];
//			angular.forEach(meetings, function(meeting){
//				var deferred = $q.defer();
//				promises.push(deferred.promise);
//				meeting.type = type;
//				meeting.rid = rid;
//				$http.post('/api/revision/saveMeetingLink',meeting)
//				.then(function(data) {
//					deferred.resolve();
//				});
//			});
//			$q.all(promises).then(callback);
        }
    };
    return linkObject;
});

App.factory("skuFactory", function ($http, $filter, $q, Notification) {
    var skuObject = {
        get: function (pid, reload) {
            return $http.get('/api/program/getSku?pid=' + pid + "&reload=" + reload);
        },
        filter: function (skus) {
            return skus.isDeleted !== true;
        },
        cancel: function (skus) {
            for (var i = skus.length; i--;) {
                var sku = skus[i];
                if (sku.isDeleted) {
                    delete sku.isDeleted;
                }
                if (sku.isNew) {
                    skus.splice(i, 1);
                }
            }
            return skus;
        },
        add: function (skus) {
            return skus.push({
                id: -1,
                aka: '',
                serdes: '',
                num: '',
                io: '',
                portconfig: '',
                desc: '',
                frequency: '',
                isNew: true
            });
        },
        save: function (pid, skus, callback) {
            var obj = {};
            obj.pid = pid;
            obj.data = skus;
            $http.post('/api/program/saveSku', obj)
                .then(function (result) {
                    Notification.then({message: result.data, delay: 1000});
                    callback();
                }, function (ret) {
                Notification.error({message: ret.data, delay: 5000, title: ret.code});
            });

//				var promises = [];
//				angular.forEach(skus, function(sku){
//					var deferred = $q.defer();
//					promises.push(deferred.promise);
//					sku.pid =pid;
//					$http.post('/api/program/saveSku', sku)
//					.then(function(result) {
//						deferred.resolve();
//					});
//				});
//				$q.all(promises).then(callback);
        },
        del: function (skus, id) {
            var filtered = $filter('filter')(skus, {id: id});
            if (filtered.length) {
                filtered[0].isDeleted = true;
            }
            return skus;
        }
    };
    return skuObject;
});

App.factory("headlineFactory", function ($http, $filter) {
    var headlineObject = {
        get: function (rid, ts, reload) {
            return $http.get('/api/revision/getHeadline?rid=' + rid + "&reload=" + reload + '&ts=' + ts);
        },
        getSnapshot: function (rid) {
            return $http.get('/api/revision/getHeadlineSnapshot?rid=' + rid);
        },
        del: function (hll, index) {
            return hll.splice(index, 1);
        },
        save: function (rid, hll, cat) {
            var content = '';
            if (cat.match(/^ipcat/i)) {
                content = hll;
            }
            else {
                angular.forEach(hll, function (headline) {
                    content += headline + "<hr>";
                });
            }
            return $http.post('/api/revision/saveHeadline', {'rid': rid, 'value': content});
        },
        parse: function (hl) {
            var headlineClickObj = {};
            //parse headline click string
            var $hl = angular.element("<span>" + hl + "</span>");
            var found = false;
            //get issue or status
            if ($hl.find('p').length > 0) {
                found = true;
                if ($hl.find('p')[0].innerText.trim().match(/^issue:/i)) {
                    headlineClickObj.headlineIssue = $hl.find('p')[0].innerText.trim().replace(/issue:\s*/i, '');
                } else if ($hl.find('p')[0].innerText.trim().match(/status:/i)) {
                    headlineClickObj.headlineStatus = $hl.find('p')[0].innerText.trim().replace(/status:\s*/i, '');
                }
            }
            if ($hl.find('p').length > 1) {
                found = true;
                if ($hl.find('p')[1].innerText.trim().match(/issue:/i)) {
                    headlineClickObj.headlineIssue = $hl.find('p')[1].innerText.trim().replace(/issue:\s*/i, '');
                } else if ($hl.find('p')[1].innerText.trim().match(/status:/i)) {
                    headlineClickObj.headlineStatus = $hl.find('p')[1].innerText.trim().replace(/status:\s*/i, '');
                }
            }
            if ($hl.find('ul').length > 0) {
                found = true;
                var $ul = angular.element($hl.find('ul')[0]);
                if ($ul.find('li').length > 0) {
                    var deptOwner = $ul.find('li')[0].innerText.trim().match(/\[([^\]]*)\]+$/g)[0].replace(/\[|\]/g, '');
                    headlineClickObj.headlineNextStep1 = $ul.find('li')[0].innerText.trim().replace(/\[([^\]]*)\]/g, '');
                    headlineClickObj.headlineDept1 = deptOwner.split(/\//)[0].trim();
                    headlineClickObj.headlineOwner1 = deptOwner.split(/\//)[1].trim();
                }
                if ($ul.find('li').length > 1) {
                    var deptOwner = $ul.find('li')[1].innerText.trim().match(/\[([^\]]*)\]+$/g)[0].replace(/\[|\]/g, '');
                    headlineClickObj.headlineNextStep2 = $ul.find('li')[1].innerText.trim().replace(/\[([^\]]*)\]/g, '');
                    headlineClickObj.headlineDept2 = deptOwner.split(/\//)[0].trim();
                    headlineClickObj.headlineOwner2 = deptOwner.split(/\//)[1].trim();
                }
                if ($ul.find('li').length > 2) {
                    var deptOwner = $ul.find('li')[2].innerText.trim().match(/\[([^\]]*)\]+$/g)[0].replace(/\[|\]/g, '');
                    headlineClickObj.headlineNextStep3 = $ul.find('li')[2].innerText.trim().replace(/\[([^\]]*)\]/g, '');
                    headlineClickObj.headlineDept3 = deptOwner.split(/\//)[0].trim();
                    headlineClickObj.headlineOwner3 = deptOwner.split(/\//)[1].trim();
                }
            }
            if (!found) {
                headlineClickObj.saveHeadlineType = "new";
            }
            else {
                headlineClickObj.saveHeadlineType = "edit";
            }
            return headlineClickObj;
        }
    };
    return headlineObject;
});

App.factory("remarkFactory", function ($http, $filter, $q, Notification) {
    var remarkObject = {
        get: function (gid, ts, reload) {
            return $http.get('/api/revision/getRemark?gid=' + gid + "&ts=" + ts + "&reload=" + reload);
        },
        save: function (gid, remark) {
            return $http.post('/api/revision/saveRemark', {'gid': gid, 'remark': remark});
        }
    };
    return remarkObject;
});

App.factory("milestoneFactory", function ($http, $timeout) {
    var milestoneObject = {
        getFrontPage: function (rid, reload) {
            return $http.get('/api/revision/getFrontPageMilestone?rid=' + rid + "&reload=" + reload);
        },
        delMilestone: function (tid) {
            return $http.post("/api/revision/deleteMilestone?tid=" + tid);
        },
        getCategory: function (rid, ts, reload) {
            return $http.get('/api/revision/getIndicatorCategoryList?rid=' + rid + "&reload=" + reload + "&ts=" + ts);
        },
        getIndicator: function (rid, gid, ts, reload) {
            return $http.get('/api/revision/getIndicatorByCategory?rid=' + rid + '&gid=' + gid + "&reload=" + reload + "&ts=" + ts);
        },

        getRemarkSnapshot: function (gid) {
            return $http.get('/api/revision/getRemarkSnapshot?gid=' + gid);
        },
        getMilestoneSnapshot: function (gid) {
            return $http.get('/api/revision/getMilestoneSnapshot?gid=' + gid);
        },
        checkEditFormShow: function (milestoneeditform, index) {
            if (typeof index == 'undefined' || index < 0) {
                //$alert({title:'Edit Error', content:'<br>Please select milestone that you would like to edit.',container: 'body',  placement:'top', type:'danger',show:true, duration:5 });
                return 'Please select milestone that you would like to edit.';
            }
            else {
                milestoneeditform.$show();
            }
            return "";
        },
        getCategoryStatus: function (milestones, tid, callback) {
            var color = 'black';
            angular.forEach(milestones, function (arr, index) {
                if (arr.id != tid) {
                    if (arr != null && typeof arr != 'undefined') {
                        if (typeof arr.current_end != 'undefined') {
                            if (typeof arr.current_end.MILESTONE != 'undefined') {
                                var dcolor = arr.current_end.MILESTONE[0]['dhstatus'];
                                if (dcolor.match(/red/i)) {
                                    color = 'red';
                                }
                                else {
                                    if (!color.match(/red/i)) {
                                        if (dcolor.match(/orange/i)) {
                                            color = 'orange';
                                        }
                                        else {
                                            if (!color.match(/orange/i)) {
                                                if (dcolor.match(/(grey|black)/i) && !color.match(/green/i)) {
                                                    color = 'black';
                                                }
                                                else {
                                                    color = 'green';
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
            callback(color);
        },
        parseRowClick: function (milestone, callback) {
            var clickObj = {};
            clickObj.milestonePlanStartDate = '';
            clickObj.milestonePlanStartComment = '';
            clickObj.milestoneActualStartDate = '';
            clickObj.milestoneActualStartComment = '';
            clickObj.milestonePlanEndDate = '';
            clickObj.milestonePlanEndComment = '';
            clickObj.milestoneActualEndDate = '';
            clickObj.milestoneActualEndComment = '';

            clickObj.milestonePlanEndSnapshot = '';
            clickObj.milestoneActualEndSnapshot = '';

            clickObj.milestonePlanStartOptionModel = '';
            clickObj.milestonePlanEndOptionModel = '';
            clickObj.milestoneActualStartOptionModel = '';
            clickObj.milestoneActualEndOptionModel = '';
            clickObj.milestoneStatus = milestone.tstatus;

            if (milestone.plan_start && milestone.plan_start.MILESTONE && milestone.plan_start.MILESTONE.length > 0) {
                if (milestone.plan_start.MILESTONE[0].value.match(/[0-9]/)) {
                    clickObj.milestonePlanStartDate = milestone.plan_start.MILESTONE[0].value;
                    clickObj.milestonePlanStartDateOptionModel = 'DATE';
                }
                else if (milestone.plan_start.MILESTONE[0].value.match(/tbd/i)) {
                    clickObj.milestonePlanStartDateOptionModel = 'TBD';
                }
                else if (milestone.plan_start.MILESTONE[0].value.match(/na/i)) {
                    clickObj.milestonePlanStartDateOptionModel = 'NA';
                }
                clickObj.milestonePlanStartComment = milestone.plan_start.MILESTONE[0].comment;
            }
            if (milestone.actual_start && milestone.actual_start.MILESTONE && milestone.actual_start.MILESTONE.length > 0) {
                if (milestone.actual_start.MILESTONE[0].value.match(/[0-9]/)) {
                    clickObj.milestoneActualStartDate = milestone.actual_start.MILESTONE[0].value;
                    clickObj.milestoneActualStartDateOptionModel = 'DATE';
                }
                else if (milestone.actual_start.MILESTONE[0].value.match(/tbd/i)) {
                    clickObj.milestoneActualStartDateOptionModel = 'TBD';
                }
                else if (milestone.actual_start.MILESTONE[0].value.match(/na/i)) {
                    clickObj.milestoneActualStartDateOptionModel = 'NA';
                }
                clickObj.milestoneActualStartComment = milestone.actual_start.MILESTONE[0].comment;
            }
            if (milestone.current_end) {
                if (milestone.current_end.MILESTONE && milestone.current_end.MILESTONE.length > 0) {
                    if (milestone.current_end.MILESTONE[0].value.match(/[0-9]/)) {
                        clickObj.milestonePlanEndDate = milestone.current_end.MILESTONE[0].value;
                        clickObj.milestonePlanEndDateOptionModel = 'DATE';
                    }
                    else if (milestone.current_end.MILESTONE[0].value.match(/tbd/i)) {
                        clickObj.milestonePlanEndDateOptionModel = 'TBD';
                    }
                    else if (milestone.current_end.MILESTONE[0].value.match(/na/i)) {
                        clickObj.milestonePlanEndDateOptionModel = 'NA';
                    }
                    clickObj.milestonePlanEndComment = milestone.current_end.MILESTONE[0].comment;
                }
                if (milestone.current_end.LAST_SNAPSHOT) {
                    clickObj.milestonePlanEndSnapshot = milestone.current_end.LAST_SNAPSHOT.value;
                }
            }
            if (milestone.actual_end && milestone.actual_end.MILESTONE && milestone.actual_end.MILESTONE.length > 0) {
                if (milestone.actual_end.MILESTONE[0].value.match(/[0-9]/)) {
                    clickObj.milestoneActualEndDate = milestone.actual_end.MILESTONE[0].value;
                    clickObj.milestoneActualEndDateOptionModel = 'DATE';
                }
                else if (milestone.actual_end.MILESTONE[0].value.match(/tbd/i)) {
                    clickObj.milestoneActualEndDateOptionModel = 'TBD';
                }
                else if (milestone.actual_end.MILESTONE[0].value.match(/na/i)) {
                    clickObj.milestoneActualEndDateOptionModel = 'NA';
                }
                clickObj.milestoneActualEndComment = milestone.actual_end.MILESTONE[0].comment;
            }
            callback(clickObj);
        }
    };
    return milestoneObject;
});

App.factory("commonFactory", function ($http, $window) {
    var uri = 'data:application/vnd.ms-excel;base64,',
        template = '<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel"' +
            ' xmlns="http://www.w3.org/TR/REC-html40"><head><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet>' +
            '<x:Name>{worksheet}</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets>' +
            '</x:ExcelWorkbook></xml><![endif]--></head><body><table>{table}</table></body></html>',
        base64 = function (s) {
            return $window.btoa(unescape(encodeURIComponent(s)));
        },
        format = function (s, c) {
            return s.replace(/{(\w+)}/g, function (m, p) {
                return c[p];
            })
        };
    var commonObject = {
        tableToExcel: function (table, workbookName, worksheetName) {
            var ctx = {worksheet: worksheetName || 'sheet', table: table.html()};
            var link = document.createElement("a");
            link.download = workbookName;
            link.href = uri + base64(format(template, ctx));
            link.click();
        },
        uploadFileToUrl: function (file, uploadUrl) {
            var fd = new FormData();
            fd.append('file', file);
            return $http.post(uploadUrl, fd, {
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            });
        },
        findTextColor: function (className, value) {
            if (typeof className != 'undefined') {
                var parts = value.split("/api/");
                var d = new Date(parseInt(parts[2], 10), parseInt(parts[1], 10) - 1, parseInt(parts[0], 10));
                var current = new Date();
                if (d > current || d.getFullYear() < 2000) {
                    if (className.match(/red/i)) {
                        return 'tred';
                    }
                    else if (className.match(/orange/i)) {
                        return 'torange';
                    }
                    else {
                        return 'tblack';
                    }
                }
                else {
                    return 'tgrey';
                }
            }
            return 'tblack';
        },
        getBrowser: function () {
            var userAgent = $window.navigator.userAgent;
            var browsers = {chrome: /chrome/i, safari: /safari/i, firefox: /firefox/i, ie: /internet explorer/i};
            for (var key in browsers) {
                if (browsers[key].test(userAgent)) {
                    return key;
                }
            }
            return 'unknown';
        },
        findBackgroundColor: function (className) {
            if (typeof className != 'undefined') {
                if (className.match(/red/i)) {
                    return 'red';
                }
                else if (className.match(/orange/i)) {
                    return 'orange';
                }
            }
            return 'green';
        },
        formatDateDisplay: function (ds) {
            if (typeof ds != 'undefined') {
                if (ds.match(/1980/i)) {
                    return '';
                }
                else if (ds.match(/1970/i)) {
                    return 'Done';
                }
                else if (ds.match(/1960/i)) {
                    return 'TBD';
                }
                else if (ds.match(/1950/i)) {
                    return 'NA';
                }
            }
            return ds;
        }
    };
    return commonObject;

});

App.factory("outlookFactory", function ($http, $filter, $sce) {
    var outlookObject = {
        get: function (rid, reload) {
            return $http.get('/api/revision/getPMOutlook?rid=' + rid + "&reload=" + reload);
        },
        save: function (rid, outlook) {
            return $http.post('/api/revision/savePMOutlook', {'rid': rid, 'value': JSON.stringify(outlook)});
        }
    };
    return outlookObject;
});

App.factory("resourceFactory", function ($http, $filter, $sce) {
    var resourceObject = {
        getMonthlyChart: function (rid, reload) {
            return $http.get('/api/resource/program/getResourceMonthlyChart?rid=' + rid + '&reload=' + reload);
        },
        getSummaryTable: function (rid, reload) {
            return $http.get('/api/resource/program/getResourceSummaryTable?rid=' + rid + '&reload=' + reload);
        },
        getSkillCompareTable: function (rid, reload) {
            return $http.get('/api/resource/program/getResourceSkillSummary?rid=' + rid + '&reload=' + reload);
        },
    };
    return resourceObject;
});
App.factory("swFactory", function ($http, $q, $filter) {
    var swObject = {
        get: function (pid, reload, hideLoadingBar) {
            return $http.get("/api/revision/getSWHeadlineList?pid=" + pid + "&reload=" + reload, {ignoreLoadingBar: hideLoadingBar});
        },
        filter: function (sws) {
            if (sws != null && typeof sws.isDeleted != 'undefined') {
                return sws.isDeleted !== true;
            }
            else {
                return sws;
            }
        },
        cancel: function (sws) {
            for (var i = sws.length; i--;) {
                var sw = sws[i];
                if (sw.isDeleted) {
                    delete sw.isDeleted;
                }
                if (sw.isNew) {
                    sws.splice(i, 1);
                }
            }
            return sws;
        },
        add: function (sws) {
            var today = new Date();
            var dd = today.getDate();
            var mm = today.getMonth() + 1; //January is 0!

            var yy = today.getFullYear().toString().substr(2, 2);
            if (dd < 10) {
                dd = '0' + dd;
            }
            if (mm < 10) {
                mm = '0' + mm;
            }
            var ts = mm + '/api/' + dd + '/api/' + yy;
            return sws.unshift({
                id: -1,
                color: 'green',
                headline: '',
                includeReport: true,
                pname: '',
                rname: '',
                ea: '',
                ga: '',
                ts: ts,
                isNew: true
            });
        },
        save: function (pid, sws, callback) {
            var promises = [];
            angular.forEach(sws, function (sw) {
                var deferred = $q.defer();
                promises.push(deferred.promise);
                sw.pid = pid;
                $http.post('/api/revision/saveSW', sw)
                    .then(function (result) {
                        deferred.resolve();
                    });
            });
            $q.all(promises).then(callback);
        },
        del: function (sws, id) {
            var filtered = $filter('filter')(sws, {id: id});
            if (filtered.length) {
                filtered[0].isDeleted = true;
            }
            return sws;
        }
    };
    return swObject;
});

App.factory("ipChipTableFactory", function ($http, $filter, $q, Notification) {
    var ipObj = {
        get: function (rid, reload) {
            return $http.get('/api/revision/getIPTable?rid=' + rid + "&reload=" + reload);
        },
        filter: function (ip) {
            return ip.isDeleted !== true;
        },
        cancel: function (ips) {
            for (var i = ips.length; i--;) {
                var ip = ips[i];
                if (ip.isDeleted) {
                    delete ip.isDeleted;
                }
                if (ip.isNew) {
                    ips.splice(i, 1);
                }
            }
            return ips;
        },
        add: function (ips) {
            if (!Array.isArray(ips)) {
                ips = [];
            }
            return ips.unshift({
                id: -1,
                rid: 0,
                pid: 0,
                displayName: '',
                isNew: true
            });
        },
        save: function (rid, ips, callback) {
            var obj = {};
            obj.rid = rid;
            obj.data = ips;
            $http.post('/api/revision/saveIPTable', obj)
                .then(function (result) {
                    Notification.then({message: result.data, delay: 1000});
                    callback();
                }, function (ret) {
                Notification.error({message: ret.data, delay: 5000, title: ret.code});
            });
//			var promises = [];
//			angular.forEach(ips, function(ip){
//				ip.rid = rid;
//				var deferred = $q.defer();
//				promises.push(deferred.promise);
//				$http.post('/api/revision/saveIPTable', ip)
//				.then(function(data) {
//					deferred.resolve();
//				});
//			});
//			$q.all(promises).then(callback);
        },
        del: function (ips, id) {
            var filtered = $filter('filter')(ips, {id: id});
            if (filtered.length) {
                filtered[0].isDeleted = true;
            }
            return ips;
        }
    };
    return ipObj;
});