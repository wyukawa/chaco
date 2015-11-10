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
            <form action="/query" method="post">
                <div class="form-group">
                    <label>SQL</label>
                    <textarea class="form-control" rows="3" cols="150" id="query" name="query">${query}</textarea>
                </div>
                <button type="submit" class="btn btn-default" id="query-submit">Execute</button>
            </form>

        <#if error?has_content>
            <div class="alert alert-danger" role="alert">${error}</div>
        </#if>

            <table class="table table-bordered">
                <thead>
                <#list rows as row>
                <tr>
                    <#list row as column>
                        <#if column??>
                            <td>${column}</td>
                        <#else>
                            <td>null</td>
                        </#if>
                    </#list>
                </tr>
                </#list>
                </thead>
            </table>
        </td>
    </tr>

<#include "include/footer.html.ftl">
