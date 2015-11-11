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
    $("#error-msg").hide();
    $("#query-results").empty();
    var query = $("#query").val();
    var requestURL = "/query";
    var requestData = {
        "query": query
    };
    var successHandler = function (data) {
        if (data.error) {
            $("#error-msg").text(data.error);
            $("#error-msg").slideDown("fast");

        } else {
            var rows = data.rows;
            var tbody = document.createElement("tbody");
            for (var i = 0; i < rows.length; i++) {
                var tr = document.createElement("tr");
                var columns = rows[i];
                for (var j = 0; j < columns.length; j++) {
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
            $("#query-results").append(tbody);
        }
    };
    $.post(requestURL, requestData, successHandler, "json");
});
