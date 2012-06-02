<!DOCTYPE HTML>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Sync Project</title>

<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="">
<meta name="author" content="">
<link rel="stylesheet"
	href="http://twitter.github.com/bootstrap/assets/css/bootstrap.css">
<style type="text/css">
body {
	padding-top: 60px;
	padding-bottom: 40px;
}
</style>
<link
	href="http://twitter.github.com/bootstrap/assets/css/bootstrap-responsive.css"
	rel="stylesheet">

<!-- fav and touch icons -->
<link rel="shortcut icon"
	href="http://twitter.github.com/bootstrap/assets/ico/favicon.ico">
<link rel="apple-touch-icon"
	href="http://twitter.github.com/bootstrap/assets/ico/apple-touch-icon.png">
<link rel="apple-touch-icon" sizes="72x72"
	href="http://twitter.github.com/bootstrap/assets/ico/apple-touch-icon-72x72.png">
<link rel="apple-touch-icon" sizes="114x114"
	href="http://twitter.github.com/bootstrap/assets/ico/apple-touch-icon-114x114.png">
	
	
	
	<script type="text/javascript">
		function doSubmit() {
			if (document.getElementById('query').value.length + document.location.href.length >= 2000) {
				//Max length, as IE has max uri length
				return true;
			} else {
				var url = [];
				if (document.location.href.charAt(document.location.href.length-1) == "/") {
					url[url.length] = '';
				} else {
					url[url.length] = document.location.href + '/';
				}
				url[url.length] = '';
				//url[url.length] = document.getElementById("query").value;
				addParam(url, "query");
				addParam(url, "mode");
				document.location.href = url.join('');
				return false;
			}
		}
		function addParam(sb, name, id) {
			if (!id) {
				id = name;
			}
			var tag = document.getElementById(id);
			sb[sb.length] = name;
			sb[sb.length] = '=';
			if (tag.type == "checkbox") {
				if (tag.checked) {
					sb[sb.length] = 'true';
				} else {
					sb[sb.length] = 'false';
				}
			} else {
				sb[sb.length] = encodeURIComponent(tag.value);
			}
			sb[sb.length] = '&';
		}
	</script>
</head>

<body>
	<div class='navbar navbar-fixed-top' style='z-index: 100;'>
		<div class='navbar-inner'>
			<div class='container'>
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span>
				</a> <a class="brand" href="/">Sync Project</a>
			</div>
		</div>
	</div>
	<div class='container'>
	
		<form action="${action}" method="POST" onsubmit="return doSubmit();">
		<!--<form action="${action}" method="POST">-->
			<textarea id="query" name="query" rows="16" cols="80"></textarea><br>
			<select id="mode" name="mode">
			  <option value="1">Log queries (rsync)</option>
			  <option value="2">Log queries (DB)</option>
			  <option value="3">Serialize graph (rsync)</option>
			  <option value="4">Log queries (GIT)</option>
			  <option value="5">Serialize graph (git)</option>
			  <option value="6">Serialize graph (DB)</option>
			</select><br>
			<input type="submit" value="Execute"/>
		</form>
		Example queries:<br>
		SELECT * WHERE {?x &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#type&gt; ?g} LIMIT 5<br>
		SELECT * WHERE {&lt;http://www.data2semantics.org/data/BRT_1889_02_T1_marked/Table_1/BH10&gt; ?x ?y} LIMIT 5 <br>
		INSERT {&lt;http://example/sub&gt; &lt;http://example/bla&gt; "test"} WHERE {} <br>
		SELECT * {&lt;http://example/sub&gt; &lt;http://example/bla&gt; ?x}
	</div>

	<!-- Javascript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script src="http://twitter.github.com/bootstrap/assets/js/jquery.js"></script>
	<script
		src="http://twitter.github.com/bootstrap/assets/js/bootstrap-transition.js"></script>
	<script
		src="http://twitter.github.com/bootstrap/assets/js/bootstrap-alert.js"></script>
	<script
		src="http://twitter.github.com/bootstrap/assets/js/bootstrap-modal.js"></script>
	<script
		src="http://twitter.github.com/bootstrap/assets/js/bootstrap-dropdown.js"></script>
	<script
		src="http://twitter.github.com/bootstrap/assets/js/bootstrap-scrollspy.js"></script>
	<script
		src="http://twitter.github.com/bootstrap/assets/js/bootstrap-tab.js"></script>
	<script
		src="http://twitter.github.com/bootstrap/assets/js/bootstrap-tooltip.js"></script>
	<script
		src="http://twitter.github.com/bootstrap/assets/js/bootstrap-popover.js"></script>
	<script
		src="http://twitter.github.com/bootstrap/assets/js/bootstrap-button.js"></script>
	<script
		src="http://twitter.github.com/bootstrap/assets/js/bootstrap-collapse.js"></script>
	<script
		src="http://twitter.github.com/bootstrap/assets/js/bootstrap-carousel.js"></script>
	<script
		src="http://twitter.github.com/bootstrap/assets/js/bootstrap-typeahead.js"></script>
</body>
</html>