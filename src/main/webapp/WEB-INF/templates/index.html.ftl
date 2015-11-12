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
                <div id="query-results-div">
                    <table class="table table-bordered" id="query-results"></table>
                </div>
            </div>
        </td>
    </tr>

<#include "include/footer.html.ftl">
