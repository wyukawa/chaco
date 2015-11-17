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

            <h4>query histories</h4>

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

        </td>
    </tr>

<#include "include/footer.html.ftl">
