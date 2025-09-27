import { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import {
  Eye, EyeOff, Building, Users, AlertCircle, CheckCircle, Clock,
  Loader2, User, Mail, Phone, Lock, UserPlus, ArrowRight, Shield, AtSign, X
} from 'lucide-react';
import toast from 'react-hot-toast';
import { motion, AnimatePresence } from 'framer-motion';
import inviteService from '../../api/inviteService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { normalizeUserFormData } from '../../utils/stringUtils';

const schema = yup.object({
  firstName: yup.string().required('First name is required').min(2, 'Must be at least 2 characters'),
  lastName: yup.string().required('Last name is required').min(2, 'Must be at least 2 characters'),
  username: yup.string()
    .required('Username is required')
    .min(3, 'Username must be between 3 and 30 characters')
    .max(30, 'Username must be between 3 and 30 characters')
    .matches(/^[a-zA-Z0-9._-]+$/, 'Username can only contain letters, numbers, dots, underscores, and hyphens'),
  email: yup.string().required('Email is required').email('Invalid email format'),
  phoneNumber: yup.string().notRequired().test('phone-validation', 'Invalid phone number', function(value) {
    if (!value || value.trim() === '') return true; // Allow empty values
    return /^\+?[1-9]\d{1,14}$/.test(value.replace(/[\s\-\(\)]/g, '')); // Clean and validate
  }),
  password: yup.string()
    .required('Password is required')
    .min(8, 'Password must be at least 8 characters')
    .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/, 
      'Must contain uppercase, lowercase, number and special character'),
  confirmPassword: yup.string()
    .required('Please confirm your password')
    .oneOf([yup.ref('password')], 'Passwords must match'),
});

const EnhancedInviteRegistration = () => {
  const [searchParams] = useSearchParams();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isValidating, setIsValidating] = useState(true);
  const [inviteData, setInviteData] = useState(null);
  const [validationError, setValidationError] = useState(null);
  const [registrationSuccess, setRegistrationSuccess] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);
  const [countdown, setCountdown] = useState(10);
  const navigate = useNavigate();
  
  const inviteCode = searchParams.get('invite');

  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
    trigger
  } = useForm({
    resolver: yupResolver(schema),
    mode: 'onBlur'
  });

  const watchedValues = watch();

  // Validate invite on component mount
  useEffect(() => {
    if (!inviteCode) {
      setValidationError('No invite code provided. Registration requires a valid invite link.');
      setIsValidating(false);
      return;
    }

    const validateInvite = async () => {
      try {
        setIsValidating(true);
        const response = await inviteService.validateInvite(inviteCode);
        
        if (response.isValid) {
          setInviteData(response);
        } else {
          setValidationError(response.message || 'Invalid invite code');
        }
      } catch (error) {
        console.error('Invite validation error:', error);
        const errorMessage = error.response?.data?.message || 'Failed to validate invite';
        setValidationError(errorMessage);
      } finally {
        setIsValidating(false);
      }
    };

    validateInvite();
  }, [inviteCode, navigate]);

  // Countdown effect for auto-redirect
  useEffect(() => {
    if (registrationSuccess && countdown > 0) {
      const timer = setTimeout(() => {
        setCountdown(countdown - 1);
      }, 1000);
      return () => clearTimeout(timer);
    }
  }, [registrationSuccess, countdown]);

  const onSubmit = async (data) => {
    if (!inviteData?.isValid) return;

    setIsLoading(true);
    try {
      // Normalize form data before sending to backend
      const normalizedData = normalizeUserFormData(data);

      await inviteService.registerWithInvite({
        ...normalizedData,
        inviteCode
      });
      
      setRegistrationSuccess(true);
      toast.success('Registration successful! Welcome to the team!');

      // Start countdown
      setCountdown(10);

      // Auto-redirect to login after 10 seconds
      setTimeout(() => {
        navigate('/login', {
          state: {
            message: 'Registration complete! Please sign in with your credentials.',
            email: data.email
          }
        });
      }, 10000);
      
    } catch (error) {
      console.error('Registration error:', error);
      const errorMessage = error.response?.data?.message || 'Registration failed. Please try again.';
      
      // Handle specific error cases
      if (errorMessage.includes('already used')) {
        setValidationError('This invite has already been used');
        toast.error('This invite has already been used');
      } else if (errorMessage.includes('expired')) {
        setValidationError('This invite has expired');
        toast.error('This invite has expired');
      } else if (errorMessage.includes('full')) {
        setValidationError('This invite has reached its user limit');
        toast.error('This invite has reached its user limit');
      } else {
        toast.error(errorMessage);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const nextStep = async () => {
    const fieldsToValidate = currentStep === 0 
      ? ['firstName', 'lastName', 'username'] 
      : ['email', 'phoneNumber'];
    
    const isValid = await trigger(fieldsToValidate);
    if (isValid) {
      setCurrentStep(currentStep + 1);
    }
  };

  const prevStep = () => {
    setCurrentStep(Math.max(0, currentStep - 1));
  };

  // Loading state while validating
  if (isValidating) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          className="text-center"
        >
          <LoadingSpinner size="lg" text="Validating your invite..." />
          <p className="text-gray-500 mt-4">Please wait while we verify your invitation</p>
        </motion.div>
      </div>
    );
  }

  // Invalid invite state
  if (validationError || !inviteData?.isValid) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="max-w-md w-full text-center"
        >
          <div className="bg-white rounded-2xl shadow-xl p-8">
            <div className="mx-auto h-16 w-16 flex items-center justify-center rounded-full bg-red-100 mb-6">
              <AlertCircle className="h-8 w-8 text-red-600" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              Invalid Invite
            </h2>
            <p className="text-gray-600 mb-6">
              {validationError || 'This invite code is invalid or has expired'}
            </p>
            
            <div className="space-y-3">
              <p className="text-sm text-gray-500 mb-4">
                Registration is only available through valid invite links. Please contact your administrator to get a new invite.
              </p>
              <Link
                to="/login"
                className="block w-full py-3 px-4 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors"
              >
                Back to Login
              </Link>
            </div>
          </div>
        </motion.div>
      </div>
    );
  }

  // Success state
  if (registrationSuccess) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4">
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          className="max-w-md w-full text-center"
        >
          <div className="bg-white rounded-2xl shadow-xl p-8">
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ delay: 0.2 }}
              className="mx-auto h-16 w-16 flex items-center justify-center rounded-full bg-green-100 mb-6"
            >
              <CheckCircle className="h-8 w-8 text-green-600" />
            </motion.div>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              Welcome to {inviteData.companyName}!
            </h2>
            <p className="text-gray-600 mb-4">
              Your account has been created successfully! Your account is now pending approval from an administrator.
            </p>
            <div className="bg-amber-50 border border-amber-200 rounded-lg p-3 mb-6">
              <p className="text-sm text-amber-800 text-center">
                <Clock className="inline-block w-4 h-4 mr-1" />
                Please wait for admin approval before you can log in
              </p>
            </div>

            <div className="bg-green-50 rounded-xl p-4 mb-6">
              <div className="text-sm text-green-800">
                <p className="font-medium mb-2">You've joined:</p>
                <div className="space-y-1">
                  <p className="flex items-center justify-center gap-2">
                    <Building size={16} />
                    {inviteData.companyName}
                  </p>
                  <p className="flex items-center justify-center gap-2">
                    <Users size={16} />
                    {inviteData.departmentName}
                  </p>
                </div>
              </div>
            </div>

            {/* Countdown and Auto-redirect notice */}
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 mb-6">
              <p className="text-sm text-blue-800 text-center">
                <Clock className="inline-block w-4 h-4 mr-1" />
                Redirecting to login in {countdown} seconds...
              </p>
            </div>

            <Link
              to="/login"
              className="inline-flex items-center justify-center gap-2 py-3 px-6 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors w-full"
              onClick={() => {
                navigate('/login', {
                  state: {
                    message: 'Registration complete! Please sign in with your credentials.',
                    email: inviteData.email
                  }
                });
              }}
            >
              Continue to Login Now
              <ArrowRight size={16} />
            </Link>
          </div>
        </motion.div>
      </div>
    );
  }

  // Multi-step registration form
  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-md mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-center mb-8"
        >
          <div className="mx-auto h-12 w-12 flex items-center justify-center rounded-full bg-blue-100 mb-4">
            <UserPlus className="h-6 w-6 text-blue-600" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">
            Join {inviteData.companyName}
          </h1>
          <p className="text-gray-600 mt-1">
            You've been invited to join the team!
          </p>
        </motion.div>

        {/* Company Info Card */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="bg-white rounded-xl shadow-sm p-4 mb-6"
        >
          <div className="flex items-center justify-between text-sm">
            <div className="flex items-center gap-2 text-blue-900">
              <Building size={16} />
              <span className="font-medium">{inviteData.companyName}</span>
            </div>
            <div className="flex items-center gap-2 text-blue-800">
              <Users size={16} />
              <span>{inviteData.departmentName}</span>
            </div>
          </div>
        </motion.div>

        {/* Progress Steps */}
        <div className="mb-6">
          <div className="flex justify-between mb-2">
            {[0, 1, 2].map((step) => (
              <div
                key={step}
                className={`flex items-center justify-center w-8 h-8 rounded-full text-sm font-medium transition-colors ${
                  step <= currentStep
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-200 text-gray-500'
                }`}
              >
                {step + 1}
              </div>
            ))}
          </div>
          <div className="flex justify-between text-xs text-gray-500">
            <span>Personal</span>
            <span>Contact</span>
            <span>Security</span>
          </div>
        </div>

        {/* Registration Form */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2 }}
          className="bg-white rounded-xl shadow-sm p-6"
        >
          <form onSubmit={handleSubmit(onSubmit)}>
            <AnimatePresence mode="wait">
              {/* Step 1: Personal Information */}
              {currentStep === 0 && (
                <motion.div
                  key="step1"
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -20 }}
                  className="space-y-4"
                >
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      First Name
                    </label>
                    <div className="relative">
                      <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                      <input
                        {...register('firstName')}
                        type="text"
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                        placeholder="John"
                      />
                    </div>
                    {errors.firstName && (
                      <p className="mt-1 text-sm text-red-600">{errors.firstName.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Last Name
                    </label>
                    <div className="relative">
                      <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                      <input
                        {...register('lastName')}
                        type="text"
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                        placeholder="Doe"
                      />
                    </div>
                    {errors.lastName && (
                      <p className="mt-1 text-sm text-red-600">{errors.lastName.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Username
                    </label>
                    <div className="relative">
                      <AtSign className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                      <input
                        {...register('username')}
                        type="text"
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                        placeholder="john.doe"
                      />
                    </div>
                    {errors.username && (
                      <p className="mt-1 text-sm text-red-600">{errors.username.message}</p>
                    )}
                    <p className="mt-1 text-xs text-gray-500">
                      3-30 characters. Letters, numbers, dots, underscores, and hyphens only.
                    </p>
                  </div>

                  <button
                    type="button"
                    onClick={nextStep}
                    disabled={!watchedValues.firstName || !watchedValues.lastName || !watchedValues.username}
                    className="w-full flex items-center justify-center gap-2 py-3 px-4 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    Continue
                    <ArrowRight size={16} />
                  </button>
                </motion.div>
              )}

              {/* Step 2: Contact Information */}
              {currentStep === 1 && (
                <motion.div
                  key="step2"
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -20 }}
                  className="space-y-4"
                >
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Email Address
                    </label>
                    <div className="relative">
                      <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                      <input
                        {...register('email')}
                        type="email"
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                        placeholder="john@example.com"
                      />
                    </div>
                    {errors.email && (
                      <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Phone Number (Optional)
                    </label>
                    <div className="relative">
                      <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                      <input
                        {...register('phoneNumber')}
                        type="tel"
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                        placeholder="+1 (555) 123-4567"
                      />
                    </div>
                    {errors.phoneNumber && (
                      <p className="mt-1 text-sm text-red-600">{errors.phoneNumber.message}</p>
                    )}
                  </div>

                  <div className="flex gap-3">
                    <button
                      type="button"
                      onClick={prevStep}
                      className="flex-1 py-3 px-4 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition-colors"
                    >
                      Back
                    </button>
                    <button
                      type="button"
                      onClick={nextStep}
                      disabled={!watchedValues.email}
                      className="flex-1 flex items-center justify-center gap-2 py-3 px-4 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                      Continue
                      <ArrowRight size={16} />
                    </button>
                  </div>
                </motion.div>
              )}

              {/* Step 3: Security */}
              {currentStep === 2 && (
                <motion.div
                  key="step3"
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -20 }}
                  className="space-y-4"
                >
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Password
                    </label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                      <input
                        {...register('password')}
                        type={showPassword ? 'text' : 'password'}
                        className="w-full pl-10 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                        placeholder="Create a strong password"
                      />
                      <button
                        type="button"
                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                        onClick={() => setShowPassword(!showPassword)}
                      >
                        {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                      </button>
                    </div>
                    {errors.password && (
                      <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
                    )}
                    <div className="mt-2 text-xs text-gray-500">
                      <div className="flex items-center gap-2">
                        <Shield size={12} />
                        Must contain uppercase, lowercase, number and special character
                      </div>
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Confirm Password
                    </label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
                      <input
                        {...register('confirmPassword')}
                        type={showConfirmPassword ? 'text' : 'password'}
                        className="w-full pl-10 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                        placeholder="Confirm your password"
                      />
                      <button
                        type="button"
                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      >
                        {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                      </button>
                    </div>
                    {errors.confirmPassword && (
                      <p className="mt-1 text-sm text-red-600">{errors.confirmPassword.message}</p>
                    )}
                  </div>

                  <div className="flex gap-3">
                    <button
                      type="button"
                      onClick={prevStep}
                      className="flex-1 py-3 px-4 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition-colors"
                    >
                      Back
                    </button>
                    <button
                      type="submit"
                      disabled={isLoading || !watchedValues.password || !watchedValues.confirmPassword}
                      className="flex-1 flex items-center justify-center gap-2 py-3 px-4 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                      {isLoading ? (
                        <>
                          <Loader2 size={16} className="animate-spin" />
                          Joining...
                        </>
                      ) : (
                        <>
                          Join Team
                          <CheckCircle size={16} />
                        </>
                      )}
                    </button>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </form>

          {/* Terms and Login Link */}
          <div className="mt-6 pt-6 border-t border-gray-200 text-center space-y-3">
            <p className="text-xs text-gray-500">
              By signing up, you agree to our Terms of Service and Privacy Policy.
            </p>
            <Link
              to="/login"
              className="text-sm text-blue-600 hover:text-blue-500 font-medium"
            >
              Already have an account? Sign in
            </Link>
          </div>
        </motion.div>
      </div>
    </div>
  );
};

export default EnhancedInviteRegistration;