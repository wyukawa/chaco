<#-- @ftlvariable name="jdbcMajorVersion" type="java.lang.String" -->
<#-- @ftlvariable name="db" type="java.lang.String" -->
<#-- @ftlvariable name="name" type="java.lang.String" -->
<#include "include/header.html.ftl">

<table class="table">
    <tr>
        <td>

            <ul id="tableMenu" class="contextMenu">
                <li><a href="#select">SELECT ... LIMIT 100</a></li>
            </ul>

            <div id="tree">
            </div>

            <script>
                var tree = chaco_tree();
            </script>
        </td>
        <td>
            <form>
                <div class="form-group">
                    <label>Query</label>
                    <textarea class="form-control" rows="3" cols="150" id="query"></textarea>
                </div>
                <button type="button" onclick="handle_execute()" class="btn btn-primary" id="query-submit">Execute</button>
            </form>

            <div class="alert alert-danger" id="error-msg"></div>
            <script>
                $("#error-msg").hide();
            </script>

            <h4>query results</h4>

            <div id="query-results-tab">
                <div style="height:500px; overflow:auto;" id="query-results-div">
                    <table class="table table-bordered" id="query-results"></table>
                </div>
            </div>

            <script>
                $(function () {
                    $("#tabs").tabs();
                });
            </script>

            <h4>query executions/query histories</h4>

            <div id="tabs">
                <ul>
                    <li><a href="#query-executions-tab">query executions</a></li>
                    <li><a href="#query-histories-tab">query histories</a></li>
                </ul>

                <div id="query-executions-tab">
                    <div style="height:500px; overflow:auto;" id="query-executions-div">
                        <table class="table table-bordered" id="query-executions"></table>
                    </div>
                </div>

                <div id="query-histories-tab">
                    <input type="text" size="20" id="search_query_histories">
                    <script>
                        $('#search_query_histories').keyup(function(){
                            if ($(this).val()) {
                                $('#query-histories tr').hide();
                                $('#query-histories tr td:nth-child(3):contains(' + this.value + ')').parent().show();
                            } else {
                                $('#query-histories tr').show();
                            }
                        });
                    </script>
                    <div>
                        <table class="table table-bordered" id="query-histories"></table>
                    </div>
                    <script>
                        update_query_histories_area();
                    </script>
                </div>

            </div>

        </td>
    </tr>

<#include "include/footer.html.ftl">
