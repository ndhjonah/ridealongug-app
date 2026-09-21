export const PHONE_DIGITS_ONLY = /^\d*$/;
const PHONE_PATTERN = /^\d{9,10}$/;
const EMAIL_PATTERN = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
const USERNAME_PATTERN = /^[A-Za-z0-9_.]{3,30}$/;
const LICENCE_PATTERN = /^[A-Za-z0-9-]{4,20}$/;

export function sanitizePhoneInput(value: string): string {
  return value.replace(/\D/g, '').slice(0, 10);
}

export function validatePhone(value: string, opts: { required?: boolean } = {}): string {
  const { required = false } = opts;
  if (!value) return required ? 'Phone number is required' : '';
  if (!PHONE_PATTERN.test(value)) return 'Phone number must be 9-10 digits, numbers only';
  return '';
}

export function validateEmail(value: string, opts: { required?: boolean } = {}): string {
  const { required = true } = opts;
  if (!value) return required ? 'Email is required' : '';
  if (!EMAIL_PATTERN.test(value)) return 'Enter a valid email address';
  return '';
}

export function validateUsername(value: string, opts: { required?: boolean } = {}): string {
  const { required = true } = opts;
  if (!value) return required ? 'Username is required' : '';
  if (!USERNAME_PATTERN.test(value)) return '3-30 characters: letters, numbers, dots or underscores only';
  return '';
}

export function validateName(value: string, label = 'This field', opts: { required?: boolean; min?: number } = {}): string {
  const { required = true, min = 2 } = opts;
  if (!value) return required ? `${label} is required` : '';
  if (value.trim().length < min) return `${label} must be at least ${min} characters`;
  if (!/^[A-Za-z ,.'-]+$/.test(value)) return `${label} can only contain letters`;
  return '';
}

export function validatePassword(value: string, opts: { required?: boolean; min?: number } = {}): string {
  const { required = true, min = 8 } = opts;
  if (!value) return required ? 'Password is required' : '';
  if (value.length < min) return `Password must be at least ${min} characters`;
  return '';
}

export function validateLicenceNumber(value: string, opts: { required?: boolean } = {}): string {
  const { required = true } = opts;
  if (!value) return required ? 'Licence number is required' : '';
  if (!LICENCE_PATTERN.test(value)) return 'Licence number should be 4-20 letters, numbers or dashes';
  return '';
}

export function validateRequired(value: unknown, label = 'This field'): string {
  if (value === undefined || value === null || String(value).trim() === '') return `${label} is required`;
  return '';
}

export function runValidators<T extends Record<string, any>>(
  values: T,
  validators: Partial<Record<keyof T, (v: any) => string>>
): Partial<Record<keyof T, string>> {
  const errors: Partial<Record<keyof T, string>> = {};
  (Object.keys(validators) as (keyof T)[]).forEach((field) => {
    const validate = validators[field];
    if (!validate) return;
    const message = validate(values[field]);
    if (message) errors[field] = message;
  });
  return errors;
}

export function hasErrors(errors: Record<string, string | undefined>): boolean {
  return Object.values(errors).some(Boolean);
}
