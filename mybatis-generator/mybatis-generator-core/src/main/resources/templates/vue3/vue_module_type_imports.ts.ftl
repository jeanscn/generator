<#list typeImports![] as dependency>
import type { ${dependency.name} } from '${dependency.path}';
</#list>
