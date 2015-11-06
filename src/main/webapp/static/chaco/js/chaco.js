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
                data: {catalog: "test", schema: schemaName},
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
        }

    });
    return tree;
});
