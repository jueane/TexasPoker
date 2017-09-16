<div id="Pager" class="Page_Cutter">
    <span class="Page_Cutter_No"><a href="${basePath}/${method!}?pageIndex=0&pageSize=10&orderBy=createDate&asc=false" >首页</a></span>
    <span class="Page_Cutter_No">
    <#if pager.getPageIndex() = 1>
    	 <a disabled="disabled">上一页</a>
    <#else>
    	 <a href="${basePath}/${method!}?pageIndex=${pager.getPageIndex()-1!}&pageSize=10&orderBy=createDate&asc=false">上一页</a>
    </#if>
    </span>
    <#assign index = 1>
    <#if pager.getPageIndex() - 7 gt 1>
    	<#assign index = pager.getPageIndex()-7>
    <#else>
    	<#assign index = 1>
    </#if>
	<#list index..pager.getPageIndexTotal() as i>
		<span class="Page_Cutter_No"><a href="${basePath}/${method!}?pageIndex=${i}&pageSize=10&orderBy=createDate&asc=false">${i}</a></span>
	</#list> 
	
    <span class="Page_Cutter_No">
    <#if pager.getPageIndexTotal() gt 1>
    	<a href="${basePath}/${method!}?pageIndex=${pager.getPageIndex()+1!}&pageSize=10&orderBy=createDate&asc=false">下一页</a></span>
     <#else>
     	<a disabled="disabled">下一页</a></span>
     </#if>
    <span class="Page_Cutter_No"><a href="${basePath}/${method!}?pageIndex=${(pager.getTotal()/pager.getPageSize()+1)?int}&pageSize=10&orderBy=createDate&asc=false">末页</a></span>
    <!--  disabled="disabled" -->
</div>
