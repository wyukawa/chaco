<#-- @ftlvariable name="jdbcMajorVersion" type="java.lang.String" -->
<#-- @ftlvariable name="db" type="java.lang.String" -->
<#-- @ftlvariable name="name" type="java.lang.String" -->
<#include "include/header.html.ftl">

<form action="/query" method="post">
    <div class="form-group">
        <label for="exampleInputEmail1">SQL</label>
        <textarea class="form-control" rows="3" name="sql"></textarea>
    </div>
    <button type="submit" class="btn btn-default">Execute</button>
</form>

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
