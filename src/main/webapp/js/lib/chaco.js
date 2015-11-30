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
                        window.editor.setValue(query);
                        $("#query-submit").click();
                    }
                });
            }
        }
    });
    return tree;
});


var removeNewLinesAndComments = (function(str){
    return str.replace(/--.*$/mg, '').split(/\r\n|\r|\n/).join(' ');
});

var handle_execute = (function () {
    $("#query-submit").attr("disabled", "disabled");
    $("#error-msg").hide();
    $("#query-results-div").remove();
    var div = $("<div></div>", {style: "height:500px; overflow:auto;", id: "query-results-div"});
    div.append($("<table></table>", {class: "table table-bordered", id: "query-results"}));
    $("#query-results-tab").append(div);
    var tr = document.createElement("tr");
    var td = document.createElement("td");
    var img = document.createElement("img");
    $(img).attr("src", "img/loading_long_48.gif");
    $(td).append(img);
    $(tr).append(td);
    $("#query-results").append(tr);
    var query = window.editor.getValue();
    var requestURL;
    if (/^\s*(with .*)?\s*(explain\s+)?select .* from .+$/i.exec(removeNewLinesAndComments(query))){
        requestURL = "/query";
    } else {
        requestURL = "/update";
    }
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
            push_query(query);
            $("#query-histories").empty();
            update_query_histories_area();
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

    if(table_id == "#running-query") {
        var kill_th = document.createElement("th");
        $(tr).append(kill_th);
    }

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
        
        if(table_id == "#running-query") {
            var session_id = columns[0];
            var kill_td = document.createElement("td");
            $(kill_td).append('<button type="button" id="query-submit" onclick="kill(' + session_id + ')" class="btn btn-primary">kill</button>');
            $(tr).append(kill_td);
        }

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

var push_query = (function (query) {
    if (!window.localStorage) return;
    var list = query_histories();
    list.unshift(query);
    set_query_histories(list.slice(0, 1000000));
});

var query_histories = (function () {
    if (!window.localStorage) return [];
    var list = [];
    try {
        var listString = window.localStorage.chaco_query_histories;
        if (listString && listString.length > 0)
            list = JSON.parse(listString);
    } catch (e) {
        set_query_histories([]);
        list = [];
    }
    return list;
});

var set_query_histories = (function (list) {
    if (!window.localStorage) return;
    window.localStorage.chaco_query_histories = JSON.stringify(list);
});

var update_query_histories_area = (function () {
    var tbody = document.createElement("tbody");
    var query_list = query_histories();
    for (var i = 0; i < query_list.length; i++) {
        var tr = document.createElement("tr");
        var copy_button = document.createElement("button");
        $(copy_button).attr("type", "button");
        $(copy_button).attr("class", "btn btn-success");
        $(copy_button).text("copy to query area");
        $(copy_button).click({query: query_list[i]}, copy_query);
        var td = document.createElement("td");
        $(td).append(copy_button);
        $(tr).append(td);
        var delete_button = document.createElement("button");
        $(delete_button).attr("type", "button");
        $(delete_button).attr("class", "btn btn-info");
        $(delete_button).text("delete");
        $(delete_button).click({index: i}, delete_query);
        var td = document.createElement("td");
        $(td).append(delete_button);
        $(tr).append(td);
        var td = document.createElement("td");
        $(td).text(query_list[i]);
        $(tr).append(td);
        $(tbody).append(tr);
    }
    $("#query-histories").append(tbody);
});

var copy_query = (function (event) {
    window.editor.setValue(event.data.query);
});

var delete_query = (function (event) {
    if (!window.localStorage) return;
    var query_list = query_histories();
    query_list.splice(event.data.index, 1);
    set_query_histories(query_list);
    $("#query-histories").empty();
    update_query_histories_area();
});

var redraw = (function () {
    $.get("/runningquery", {}, function (data) {
        $("#running-query-div").remove();
        var div = $("<div></div>", {style: "overflow:auto;", id: "running-query-div"});
        div.append($("<h4>Running Query</h4>"))
        div.append($("<table></table>", {class: "table table-bordered", id: "running-query"}));
        $("#query-executions-tab").append(div);
        if (data.error) {
            $("#error-msg").text(data.error);
            $("#error-msg").slideDown("fast");
            $("#running-query").empty();
        } else {
            $("#running-query").empty();
            var columnNames = data.columnNames;
            var rows = data.rows;
            if(rows.length > 0) {
                create_table("#running-query", columnNames, rows);
            }
        }
    });

    $.get("/donequery", {}, function (data) {
        $("#done-query-div").remove();
        var div = $("<div></div>", {style: "overflow:auto;", id: "done-query-div"});
        div.append($("<h4>Done Query</h4>"))
        div.append($("<table></table>", {class: "table table-bordered", id: "done-query"}));
        $("#query-executions-tab").append(div);
        if (data.error) {
            $("#error-msg").text(data.error);
            $("#error-msg").slideDown("fast");
            $("#done-query").empty();
        } else {
            $("#done-query").empty();
            var columnNames = data.columnNames;
            var rows = data.rows;
            create_table("#done-query", columnNames, rows);
        }
    });

});

var kill = (function (session_id) {
    $.post("/update", {query: "DROP SESSION " + session_id}, function (data) {
        if (data.error) {
            $("#error-msg").text(data.error);
            $("#error-msg").slideDown("fast");
        }
    });
});

var query_clear = (function () {
    window.editor.setValue("");
});