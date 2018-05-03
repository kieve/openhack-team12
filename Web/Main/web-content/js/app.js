$(document).foundation();

createServerButton = function(event) {
	console.log("Test: " + document.getElementById("createServerName").value);
	$.ajax({
		url: '/rest/admin/minecraft/' + document.getElementById("createServerName").value,
		type: 'PUT',   //type is any HTTP method
		data: {
		},
		success: function () {
			console.log("ok");
			location.reload();
		},
		error: function(p1, p2) {
			console.log(p1, p2);
		}
	});
};

deleteServer = function(name) {
	$.ajax({
		url: '/rest/admin/minecraft/' + name,
		type: 'DELETE',   //type is any HTTP method
		data: {
		},
		success: function () {
			console.log("ok");
			location.reload();
		},
		error: function(p1, p2) {
			console.log(p1, p2);
		}
	});
};

