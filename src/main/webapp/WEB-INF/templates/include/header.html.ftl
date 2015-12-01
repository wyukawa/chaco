<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <title>chaco</title>
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/ui.dynatree.css" rel="stylesheet">
    <link href="css/jquery.contextMenu.css" rel="stylesheet">
    <link href="css/jquery-ui.min.css" rel="stylesheet">
    <link href="css/codemirror.css" rel="stylesheet">
    <link href="css/show-hint.css" rel="stylesheet">
    <script src="js/lib/jquery.js"></script>
    <script src="js/lib/jquery-ui.min.js"></script>
    <script src="js/lib/bootstrap.min.js"></script>
    <script src="js/lib/jquery.dynatree.min.js"></script>
    <script src="js/lib/jquery.contextMenu-custom.js"></script>
    <script src="js/lib/jquery.tablefix_1.0.1.js"></script>
    <script src="js/lib/codemirror.js"></script>
    <script src="js/lib/show-hint.js"></script>
    <script src="js/lib/sql.js"></script>
    <script src="js/lib/sql-hint.js"></script>
    <script src="js/lib/chaco.js"></script>
    <script>
        $(function(){
            follow_current_uri();
            window.addEventListener("popstate", function (event) {
                if (event.state === null || event.state === undefined || event.state.length != 32) {
                    return;
                }
                var queryid = event.state;
                follow_current_uri_query(queryid);
            }, false);
        });
    </script>

</head>
<body>

<div class="navbar navbar-inverse navbar-static-top">
    <div class="container-full">
        <div class="navbar-header">
            <a class="navbar-brand" href="/">chaco</a>
        </div>
    </div>
</div>

