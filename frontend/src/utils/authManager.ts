/**
 * 认证管理器
 * 负责管理应用的认证状态和API实例初始化
 */

// 初始化所有API实例的拦截器
export const initAuth = () => {
  // 这个函数会在main.ts中调用，确保在应用启动时正确初始化
  console.log('Auth manager initialized');
};

// 获取当前用户的认证token
export const getAuthToken = (): string | null => {
  return localStorage.getItem('token') || sessionStorage.getItem('token');
};

// 获取当前用户角色
export const getUserRole = (): number => {
  const roleStr = localStorage.getItem('userRole') || sessionStorage.getItem('userRole');
  return roleStr ? parseInt(roleStr, 10) : 0;
};

// 检查用户是否已认证
export const isAuthenticated = (): boolean => {
  return !!getAuthToken();
};

// 检查用户是否为老师角色
export const isTeacher = (): boolean => {
  return getUserRole() === 1;
};

// 检查用户是否为学生角色
export const isStudent = (): boolean => {
  return getUserRole() === 2;
};