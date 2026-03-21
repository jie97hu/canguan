import type { AppRole, RouteAccessMeta } from '@/types/auth'
import type { NavigationNode } from '@/types/navigation'

export function hasRoleAccess(allowedRoles: AppRole[] | undefined, role: AppRole | null) {
  if (!allowedRoles || allowedRoles.length === 0) {
    return true
  }
  if (!role) {
    return false
  }
  return allowedRoles.includes(role)
}

export function resolveHomePath(role: AppRole | null) {
  return role === 'OWNER' ? '/dashboard' : '/expenses'
}

export function isRoutePublic(meta: Partial<RouteAccessMeta>) {
  return meta.public === true
}

function filterNode(node: NavigationNode, role: AppRole | null): NavigationNode | null {
  if (!hasRoleAccess(node.roles, role)) {
    return null
  }

  if (node.children && node.children.length > 0) {
    const children = node.children
      .map((child) => filterNode(child, role))
      .filter((item): item is NavigationNode => Boolean(item))

    if (children.length === 0 && !node.path) {
      return null
    }

    return {
      ...node,
      children,
    }
  }

  return { ...node }
}

export function filterNavigationTree(tree: NavigationNode[], role: AppRole | null) {
  return tree
    .map((node) => filterNode(node, role))
    .filter((item): item is NavigationNode => Boolean(item))
}
