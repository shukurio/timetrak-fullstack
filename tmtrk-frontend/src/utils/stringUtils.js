/**
 * Normalizes a username by converting to lowercase and trimming whitespace
 * @param {string} username - The username to normalize
 * @returns {string} - The normalized username
 */
export const normalizeUsername = (username) => {
  if (!username) return '';
  return username.trim().toLowerCase();
};

/**
 * Capitalizes the first letter of each word and converts the rest to lowercase
 * @param {string} name - The name to capitalize
 * @returns {string} - The capitalized name
 */
export const capitalizeName = (name) => {
  if (!name) return '';
  return name
    .trim()
    .toLowerCase()
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

/**
 * Normalizes form data for user registration
 * @param {Object} formData - The form data to normalize
 * @returns {Object} - The normalized form data
 */
export const normalizeUserFormData = (formData) => {
  return {
    ...formData,
    username: normalizeUsername(formData.username),
    firstName: capitalizeName(formData.firstName),
    lastName: capitalizeName(formData.lastName),
    email: formData.email?.trim().toLowerCase() || ''
  };
};