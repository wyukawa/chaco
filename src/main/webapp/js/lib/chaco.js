var chaco_tree = (function () {
    var tree = $("#tree").dynatree({
        initAjax: {
            type: "GET",
            url: "schemaNames"
        },
        postProcess: function (data, dataType) {
            schemaNames = data["schemaNames"];
            for (var i = 0; i < schemaNames.length; i++) {
                var schemaName = schemaNames[i];
                var rootNode = $("#tree").dynatree("getRoot");
                rootNode.addChild({
                    title: schemaName,
                    key: schemaName,
                    isFolder: true,
                    isLazy: true,
                    schemaName: schemaName
                });
            }
        },
        onLazyRead: function (node) {
            if (node.data.schemaName) {
                var schemaName = node.data.key;
                $.ajax({
                    url: "tableNames",
                    data: {schema: schemaName},
                    type: "GET",
                    dataType: "json"
                }).done(function (data) {
                    if (data["error"]) {
                        console.log(data["error"]);
                        return;
                    }
                    var tableNames = data["tableNames"];

                    for (var i = 0; i < tableNames.length; i++) {
                        var tableName = tableNames[i];
                        node.addChild({title: tableName, key: tableName, isLazy: true, isFolder: true, table: tableName});
                    }
                    node.setLazyNodeStatus(DTNodeStatus_Ok);
                }).fail(function () {
                    node.data.isLazy = false;
                    node.setLazyNodeStatus(DTNodeStatus_Ok);
                    node.render();
                });
            } else if (node.parent.data.schemaName) {
                var schemaName = node.parent.data.schemaName;
                var tableName = node.data.key;
                $.ajax({
                    url: "columnNames",
                    data: {schema: schemaName, table: tableName},
                    type: "GET",
                    dataType: "json"
                }).done(function (data) {
                    if (data["error"]) {
                        console.log(data["error"]);
                        return;
                    }
                    var columnNames = data["columnNames"];

                    for (var i = 0; i < columnNames.length; i++) {
                        var columnName = columnNames[i];
                        node.addChild({title: columnName, key: columnName, isLazy: true, isFolder: false, table: columnName});
                    }
                    node.setLazyNodeStatus(DTNodeStatus_Ok);
                }).fail(function () {
                    node.data.isLazy = false;
                    node.setLazyNodeStatus(DTNodeStatus_Ok);
                    node.render();
                });

            }

        },
        onCreate: function (node, span) {
            if (node.data.table) {
                $(span).contextMenu({menu: "tableMenu"}, function (action, el, pos) {
                    table = node.data.table;
                    schemaName = node.parent.data.schemaName;
                    if (action === "select") {
                        query = "SELECT * FROM " + schemaName + "." + table + " LIMIT 100";
                        $("#query").val(query);
                        $("#query-submit").click();
                    }
                });
            }
        }
    });
    return tree;
});

var handle_execute = (function () {
    $("#query-submit").attr("disabled", "disabled");
    $("#error-msg").hide();
    $("#query-results-div").remove();
    var div = $("<div></div>", {style: "overflow:auto;", id: "query-results-div"});
    div.append($("<table></table>", {class: "table table-bordered", id: "query-results"}));
    $("#query-results-tab").append(div);
    var tr = document.createElement("tr");
    var td = document.createElement("td");
    var img = document.createElement("img");
    $(img).attr("src", "img/loading_long_48.gif");
    $(td).append(img);
    $(tr).append(td);
    $("#query-results").append(tr);
    var query = $("#query").val();
    var requestURL = "/query";
    var requestData = {
        "query": query
    };
    var successHandler = function (data) {
        $("#query-submit").removeAttr("disabled");
        if (data.error) {
            $("#error-msg").text(data.error);
            $("#error-msg").slideDown("fast");
            $("#query-results").empty();
        } else {
            $("#query-results").empty();
            var columnNames = data.columnNames;
            var rows = data.rows;
            create_table("#query-results", columnNames, rows);
        }
    };
    $.post(requestURL, requestData, successHandler, "json");
});

var create_table = (function (table_id, columnNames, rows) {
    var thead = document.createElement("thead");
    var tr = document.createElement("tr");
    for (var i = 0; i < columnNames.length; ++i) {
        var th = document.createElement("th");
        $(th).text(columnNames[i]);
        $(tr).append(th);
    }
    $(thead).append(tr);
    $(table_id).append(thead);
    var tbody = document.createElement("tbody");
    for (var i = 0; i < rows.length; ++i) {
        var tr = document.createElement("tr");
        var columns = rows[i];
        for (var j = 0; j < columns.length; ++j) {
            var td = document.createElement("td");
            if (typeof columns[j] == "object") {
                $(td).text(JSON.stringify(columns[j]));
            } else {
                $(td).text(columns[j]);
            }
            $(tr).append(td);
        }
        $(tbody).append(tr);
    }
    $(table_id).append(tbody);
    $(table_id).tablefix({height: 800, fixRows: 1});

});