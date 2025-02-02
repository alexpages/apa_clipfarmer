<xsl:template match="checkstyle/file/error">
    <tr>
        <td><xsl:value-of select="@source"/></td> <!-- Rule Name -->
        <td><xsl:value-of select="@line"/></td>   <!-- Line Number -->
        <td><xsl:value-of select="@message"/></td> <!-- Violation Message -->
    </tr>
</xsl:template>