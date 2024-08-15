(function () {
  'use strict';

  var myApp = angular.module('LogLevelModifierApp', []);

  myApp.controller('LogLevelModifierController', ['$scope', '$http', function($scope, $http) {
	   console.log('Controller');


	   $scope.getLoggers = function() {
			$http({
		        method  : "GET",
		        withCredentials: true,
		        xsrfHeaderName : "X-XSRF-TOKEN",
		        xsrfCookieName : "CSRF-TOKEN",
		        url : PluginHelper.getPluginRestUrl('logLevelModifier') + '/getLoggers'
		    }).then(function mySuccess(response) {
		    		$scope.loggers = response.data;
				    $scope.filterLoggers();
		    }, function myError(response) {
		      alert("We hit an error initializing the GET request");
		    });
		};

	   $scope.isDisabled = function(EffectiveLevel, level) {
		   if(EffectiveLevel.toUpperCase() == level.toUpperCase()) {
			   return true;
		   } else {
			   return false;
		   }
	   }

	   $scope.filterLoggers = function () {
		   console.log('Running filterLoggers. Level set to '+ $scope.selectedLevel+" Filter string: "+$scope.logFilter);


		   if($scope.logFilter) {
			   var filter = $scope.logFilter.toUpperCase();
		   } else {
			   var filter = "";
		   }

		   for (var i = 0; i < $scope.loggers.length; i++) {
			  var obj = $scope.loggers[i];

			  var loggerName = obj.LoggerName.toUpperCase();

			  if($scope.selectedLevel == "All levels") {

				  if(loggerName.indexOf(filter) > -1) {
					  obj.show = true;
				  } else {
					  obj.show = false;
				  }
			  } else {
				  if(obj.EffectiveLevel.toUpperCase() == $scope.selectedLevel.toUpperCase()) {
					  if(loggerName.indexOf(filter) > -1) {
						  obj.show = true;
					  } else {
						  obj.show = false;
					  }
				  }  else {
					  obj.show = false;
				  }
			  }
		   }
	   }

	   $scope.clearFilter = function() {
		   $scope.logFilter = "";
		   $scope.filterLoggers();
	   }

	   $scope.levels = ["trace","debug","info","warn","error","fatal","off"];
	   $scope.selectedLevel = "All levels";

	   $scope.setLogLevel = function(targetLoggerName, targetLevel) {

      $http({
            method  : "GET",
            withCredentials: true,
            xsrfHeaderName : "X-XSRF-TOKEN",
            xsrfCookieName : "CSRF-TOKEN",
            params : { lName: targetLoggerName, level: targetLevel },
            url : PluginHelper.getPluginRestUrl('logLevelModifier') + '/setLogLevel'
        }).then(function mySuccess(response) {

            if(response.data == "OK") {
              for (var i = 0; i < $scope.loggers.length; i++) {
               var obj = $scope.loggers[i];
               if(obj.LoggerName == targetLoggerName) {
                 obj.EffectiveLevel = targetLevel.toUpperCase();


                 if($scope.selectedLevel !== "All levels") {
                   if(obj.EffectiveLevel.toUpperCase() !== $scope.selectedLevel.toUpperCase()) {
                     obj.show = false;
                   }
                 }
               }
              }
            }

        }, function myError(response) {
          alert("We hit an error initializing the GET request");
      });
	   }

	   $scope.getLoggers();
  }]);
}());