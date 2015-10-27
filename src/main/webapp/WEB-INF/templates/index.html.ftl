<#-- @ftlvariable name="jdbcMajorVersion" type="java.lang.String" -->
<#-- @ftlvariable name="db" type="java.lang.String" -->
<#-- @ftlvariable name="name" type="java.lang.String" -->
<#include "include/header.html.ftl">

<form action="/query" method="post">
    <div class="form-group">
        <label>SQL</label>
        <textarea class="form-control" rows="3" name="query">${query}</textarea>
    </div>
    <button type="submit" class="btn btn-default">Execute</button>
</form>

<#if error?has_content>
    <div class="alert alert-danger" role="alert">${error}</div>
</#if>

<table class="table table-bordered">
    <thead>
        <#list rows as row>
            <tr>
            <#list row as column>
                <td>${column}</td>
            </#list>
            </tr>
        </#list>
    </thead>
</table>

<#include "include/footer.html.ftl">
