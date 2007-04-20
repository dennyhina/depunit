<?xml version="1.0"?>
<!--
This is the page that will allow the user to add a new project
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output indent="yes" method="html" standalone="yes" omit-xml-declaration="yes"/>


<xsl:template match="test_results">
<html>
<head>

<script type="text/javascript">
function toggle(menuId)
	{
	var s = document.getElementById(menuId);
	if (!s)
		return;
	if (s.style.display == "none")
		{
		s.style.display = "inline";
		}
	else
		{
		s.style.display = "none";
		}
	}
</script>

<style type="text/css">

body
	{
	font-size: 10pt;
	}
	
td
	{
	font-size: 10pt;
	}

td.numeric
	{
	text-align: center;
	width: 80px;
	}
	
.error
	{
	background-color: red;
	}
	
table
	{
	border-collapse: collapse;
	}

</style>

</head>
<body>
Report Summary<br/>
Total Test Methods <xsl:value-of select="summary/@total"/><br/>
Tests Failed <xsl:value-of select="summary/@failed"/><br/>
Tests Passed <xsl:value-of select="summary/@passed"/><br/>
Tests Skipped <xsl:value-of select="summary/@skipped"/><br/>
Tests Not Ran <xsl:value-of select="summary/@not_ran"/><br/>

<table style="width: 80%; margin-left: .75in; margin-top: .25in;">
	<thead>
		<th>Class</th>
		<th>Method</th>
		<th>Status</th>
	</thead>
	<tbody>
<xsl:for-each select="test">
		<xsl:choose>
			<xsl:when test="@status = 'failed'">
				<tr class="error">
					<xsl:attribute name="onclick">
						javascript:toggle('<xsl:value-of select="@class"/>_<xsl:value-of select="@method"/>')
					</xsl:attribute>
					<td><xsl:value-of select="@class"/></td>
					<td><xsl:value-of select="@method"/></td>
					<td><xsl:value-of select="@status"/></td>
				</tr>
			</xsl:when>
			<xsl:otherwise>
				<tr>
					<td><xsl:value-of select="@class"/></td>
					<td><xsl:value-of select="@method"/></td>
					<td><xsl:value-of select="@status"/></td>
				</tr>
			</xsl:otherwise>
		</xsl:choose>
		
</xsl:for-each>
		
	</tbody>
</table>

<xsl:for-each select="test">
	<xsl:if test="@status = 'failed'">
		<div style="display: none">
			<xsl:attribute name="id">
				<xsl:value-of select="@class"/>_<xsl:value-of select="@method"/>
			</xsl:attribute>
			<xsl:value-of select="error/message"/>
			<table>
				<thead>
					<th>File</th>
					<th>Line</th>
					<th>Method</th>
					<th>Class</th>
				</thead>
				<tbody>
				<xsl:for-each select="error/stack/trace">
					<tr>
						<td><xsl:value-of select="@fileName"/></td>
						<td><xsl:value-of select="@lineNumber"/></td>
						<td><xsl:value-of select="@methodName"/></td>
						<td><xsl:value-of select="@className"/></td>
					</tr>
				</xsl:for-each>
				</tbody>
			</table>
		</div>
	</xsl:if>
</xsl:for-each>

</body>
</html>
</xsl:template>
	
</xsl:stylesheet>
