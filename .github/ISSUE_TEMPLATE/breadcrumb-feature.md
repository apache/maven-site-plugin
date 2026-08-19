---
name: Generate breadcrumbs automatically from site descriptor menu hierarchy
about: Feature request for automatic breadcrumb generation from menu hierarchy
title: Generate breadcrumbs automatically from site descriptor menu hierarchy
labels: enhancement, feature
---

## Feature Request: Generate breadcrumbs automatically from site descriptor menu hierarchy

### Context
The Apache Maven project is consolidating the Doxia website into the maven-site repository (see [maven-site #1645](https://github.com/apache/maven-site/issues/1645)). During this consolidation, the Doxia navigation menus are being merged into the main Maven site descriptor, but breadcrumbs must still be manually maintained.

A comment from @hboutemy in the issue notes:
> "the only aspect we're losing is the breadcrumb in doxia-site = 'Apache > Maven > Doxia > Introduction' in https://maven.apache.org/doxia/index.html will become 'Apache > Maven > Introduction'"
>
> "detecting breadcrumbs from menu hierarchy is a feature I want us to add to Maven Site Plugin one day, this will be the right time to work together on this, that will add the breadcrumb back in the future (TBD)"

### Current Limitations
Users currently must:
1. Manually define breadcrumbs in site.xml for each module/section
2. Keep breadcrumb definitions synchronized with menu hierarchies
3. Handle complex breadcrumb inheritance in multi-module projects
4. Deal with inconsistencies between menu and breadcrumb URLs

Related existing issues show users struggle with breadcrumb management:
- #1050 - Allow skipping auto-generated breadcrumb item for parent module
- #674 - Make it possible to remove breadcrumbs in child projects
- #701 - Breadcrumb inheritance disappears in presence of menu in parent

### Proposed Solution
Add a feature to Maven Site Plugin that automatically generates breadcrumbs from the site descriptor's menu hierarchy:

**Behavior:**
1. **Analyze menu structure**: Parse `<menu>` elements in site.xml to understand site hierarchy
2. **Generate breadcrumb path**: Create a breadcrumb trail based on the current page's position in the menu
3. **Fallback behavior**: Only generate breadcrumbs if none are explicitly defined in site.xml
4. **Preserve explicit definitions**: User-defined breadcrumbs take precedence over auto-generated ones
5. **Support menu references**: Handle `<menu ref="...">` constructs (e.g., `ref="parent"`, `ref="modules"`)

**Example Use Case:**
```xml
<!-- site.xml structure -->
<site>
  <body>
    <menu name="Apache">
      <item name="Home" href="/index.html"/>
    </menu>
    <menu name="Maven">
      <item name="Documentation" href="/docs/index.html"/>
      <menu name="Doxia">
        <item name="Introduction" href="index.html"/>
        <item name="Architecture" href="architecture.html"/>
      </menu>
    </menu>
  </body>
</site>
```

For a page at `/doxia/architecture.html`, the plugin would automatically generate:
- "Apache > Maven > Doxia > Architecture" breadcrumbs (derived from menu hierarchy)
- No need for explicit breadcrumbs definition

### Benefits
- **Reduces duplication**: Single source of truth for site structure
- **Improves maintainability**: Changes to menu structure automatically reflect in breadcrumbs
- **Solves multi-module complexity**: Especially useful when merging sites with different structures
- **Backward compatible**: Explicit breadcrumbs take precedence
- **Follows DRY principle**: Don't Repeat Yourself

### Implementation Approach
- Add optional configuration:
  ```xml
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-site-plugin</artifactId>
    <configuration>
      <generateBreadcrumbsFromMenus>true</generateBreadcrumbsFromMenus>
    </configuration>
  </plugin>
  ```
- Leverage existing Doxia Sitetools decoration model for menu analysis
- Implement in site rendering pipeline before template processing

### Acceptance Criteria
- [ ] Breadcrumbs are automatically generated from menu hierarchy when no explicit breadcrumbs are defined
- [ ] Generated breadcrumbs reflect the logical site structure
- [ ] Explicit breadcrumbs in site.xml take precedence over auto-generated ones
- [ ] Feature can be enabled/disabled via configuration
- [ ] Works with multi-module projects and menu references
- [ ] Backward compatible with existing sites
- [ ] Unit and integration tests added
- [ ] Documentation updated with examples

### Related Issues
- [maven-site #1645](https://github.com/apache/maven-site/issues/1645) - Move the Doxia site into maven-site (primary use case)
