/** Khớp `UserRole.name()` từ backend Java. */
export const UserRole = {
  CUSTOMER: 'CUSTOMER',
  SHOP_OWNER: 'SHOP_OWNER',
  ADMIN: 'ADMIN',
} as const;

export type UserRoleType = (typeof UserRole)[keyof typeof UserRole];
