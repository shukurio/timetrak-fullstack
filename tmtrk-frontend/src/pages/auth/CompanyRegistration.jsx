import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Building2, User, Mail, Phone, Eye, EyeOff,
  CheckCircle, ArrowRight, ArrowLeft, Loader2, AlertCircle,
  Users, Shield, Briefcase, Clock
} from 'lucide-react';
import toast from 'react-hot-toast';
import { motion, AnimatePresence } from 'framer-motion';
import * as yup from 'yup';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import companyService from '../../api/companyService';
import { normalizeUserFormData } from '../../utils/stringUtils';

// Validation schemas for each step
const companySchema = yup.object({
  name: yup.string()
    .required('Company name is required')
    .min(2, 'Company name must be between 2 and 100 characters')
    .max(100, 'Company name must be between 2 and 100 characters'),
  code: yup.string()
    .required('Company code is required')
    .min(3, 'Company code must be between 3 and 10 characters')
    .max(10, 'Company code must be between 3 and 10 characters')
    .matches(/^[A-Z0-9]+$/, 'Company code can only contain uppercase letters and numbers'),
  description: yup.string()
    .max(500, 'Description must not exceed 500 characters'),
  isActive: yup.boolean()
});

const adminSchema = yup.object({
  firstName: yup.string()
    .required('First name is required')
    .min(2, 'First name must be between 2 and 50 characters')
    .max(50, 'First name must be between 2 and 50 characters'),
  lastName: yup.string()
    .required('Last name is required')
    .min(2, 'Last name must be between 2 and 50 characters')
    .max(50, 'Last name must be between 2 and 50 characters'),
  username: yup.string()
    .required('Username is required')
    .min(3, 'Username must be between 3 and 30 characters')
    .max(30, 'Username must be between 3 and 30 characters')
    .matches(/^[a-zA-Z0-9._-]+$/, 'Username can only contain letters, numbers, dots, underscores, and hyphens'),
  email: yup.string()
    .required('Email is required')
    .email('Please provide a valid email address')
    .max(100, 'Email cannot exceed 100 characters'),
  phoneNumber: yup.string()
    .required('Phone number is required')
    .matches(/^\+?[1-9]\d{1,14}$/, 'Phone number must be in valid international format (e.g., +1234567890)'),
  password: yup.string()
    .required('Password is required')
    .min(8, 'Password must be between 8 and 128 characters')
    .max(128, 'Password must be between 8 and 128 characters')
    .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]+$/, 
      'Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character'),
  confirmPassword: yup.string()
    .required('Please confirm your password')
    .oneOf([yup.ref('password')], 'Passwords must match'),
});

const CompanyRegistration = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [registrationSuccess, setRegistrationSuccess] = useState(false);
  const [companyData, setCompanyData] = useState(null);

  // Forms for each step
  const companyForm = useForm({
    resolver: yupResolver(companySchema),
    mode: 'onChange'
  });

  const adminForm = useForm({
    resolver: yupResolver(adminSchema),
    mode: 'onChange'
  });


  const steps = [
    {
      number: 1,
      title: 'Company Information',
      description: 'Tell us about your company',
      icon: Building2,
      form: companyForm
    },
    {
      number: 2, 
      title: 'Admin Account',
      description: 'Create your admin account',
      icon: Shield,
      form: adminForm
    }
  ];

  const handleCompanySubmit = (data) => {
    setCompanyData(data);
    setCurrentStep(2);
  };

  const handleAdminSubmit = async (adminData) => {
    setIsLoading(true);

    try {
      // Normalize admin data
      const normalizedAdminData = normalizeUserFormData(adminData);

      const registrationData = {
        // Company data matching CompanyRequestDTO
        company: {
          name: companyData.name,
          code: companyData.code,
          description: companyData.description || null,
          isActive: true
        },

        // Admin data matching EmployeeRequestDTO
        admin: {
          firstName: normalizedAdminData.firstName,
          lastName: normalizedAdminData.lastName,
          username: normalizedAdminData.username,
          email: normalizedAdminData.email,
          phoneNumber: adminData.phoneNumber?.replace(/[\s-()]/g, '') || '', // Clean phone number
          password: adminData.password
          // companyId will be set by backend
          // departmentId is optional for admin
        }
      };

      await companyService.registerCompany(registrationData);
      
      setRegistrationSuccess(true);
      toast.success('Company registered successfully!');
      
      // Redirect to login after 3 seconds
      setTimeout(() => {
        navigate('/login');
      }, 3000);
      
    } catch (error) {
      console.error('Registration error:', error);
      toast.error(error.response?.data?.message || 'Failed to register company. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const nextStep = () => {
    if (currentStep < steps.length) {
      setCurrentStep(currentStep + 1);
    }
  };

  const prevStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  // Success screen
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
              Welcome to TimeTrak!
            </h2>
            <p className="text-gray-600 mb-6">
              Your company has been registered successfully. You can now log in with your admin account.
            </p>
            
            <div className="bg-green-50 rounded-xl p-4 mb-6">
              <div className="text-sm text-green-800">
                <p className="font-medium mb-2">Registration Complete:</p>
                <div className="space-y-1">
                  <p className="flex items-center justify-center gap-2">
                    <Building2 size={16} />
                    {companyData?.name}
                  </p>
                  <p className="flex items-center justify-center gap-2">
                    <Shield size={16} />
                    Admin account created
                  </p>
                </div>
              </div>
            </div>

            <div className="flex items-center justify-center text-sm text-gray-500 mb-4">
              <Clock size={16} className="mr-2" />
              Redirecting to login in 3 seconds...
            </div>

            <Link
              to="/login"
              className="inline-flex items-center gap-2 py-3 px-6 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors"
            >
              <Shield size={18} />
              Login Now
            </Link>
          </div>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center">
          <div className="flex items-center">
            <Clock className="h-8 w-8 text-blue-600 mr-3" />
            <span className="text-2xl font-bold text-gray-900">TimeTrak</span>
          </div>
        </div>
        <h2 className="mt-6 text-center text-3xl font-bold text-gray-900">
          Create Your Company Account
        </h2>
        <p className="mt-2 text-center text-sm text-gray-600">
          Get started with TimeTrak for your business
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-2xl">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          {/* Progress Steps */}
          <div className="mb-8">
            <div className="flex items-center justify-between">
              {steps.map((step, index) => {
                const Icon = step.icon;
                const isActive = currentStep === step.number;
                const isCompleted = currentStep > step.number;
                
                return (
                  <div key={step.number} className="flex items-center">
                    <div className={`flex items-center justify-center w-10 h-10 rounded-full border-2 transition-colors ${
                      isCompleted 
                        ? 'bg-green-100 border-green-500 text-green-600'
                        : isActive
                        ? 'bg-blue-100 border-blue-500 text-blue-600'
                        : 'bg-gray-100 border-gray-300 text-gray-400'
                    }`}>
                      {isCompleted ? (
                        <CheckCircle className="w-5 h-5" />
                      ) : (
                        <Icon className="w-5 h-5" />
                      )}
                    </div>
                    <div className="ml-3">
                      <p className={`text-sm font-medium ${
                        isActive ? 'text-blue-600' : isCompleted ? 'text-green-600' : 'text-gray-500'
                      }`}>
                        {step.title}
                      </p>
                      <p className="text-xs text-gray-500">{step.description}</p>
                    </div>
                    {index < steps.length - 1 && (
                      <div className={`flex-1 h-0.5 mx-4 ${
                        isCompleted ? 'bg-green-500' : 'bg-gray-300'
                      }`} />
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          <AnimatePresence mode="wait">
            {/* Step 1: Company Information */}
            {currentStep === 1 && (
              <motion.div
                key="step1"
                initial={{ opacity: 0, x: 50 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -50 }}
                className="space-y-6"
              >
                <form onSubmit={companyForm.handleSubmit(handleCompanySubmit)} className="space-y-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Company Name *
                    </label>
                    <input
                      {...companyForm.register('name')}
                      type="text"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="Your Company Name"
                    />
                    {companyForm.formState.errors.name && (
                      <p className="mt-1 text-sm text-red-600">{companyForm.formState.errors.name.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Company Code *
                    </label>
                    <input
                      {...companyForm.register('code')}
                      type="text"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="ACME"
                      style={{ textTransform: 'uppercase' }}
                      onChange={(e) => {
                        e.target.value = e.target.value.toUpperCase();
                        companyForm.setValue('code', e.target.value);
                      }}
                    />
                    {companyForm.formState.errors.code && (
                      <p className="mt-1 text-sm text-red-600">{companyForm.formState.errors.code.message}</p>
                    )}
                    <div className="mt-2 p-3 bg-blue-50 rounded-lg">
                      <p className="text-xs font-medium text-blue-900 mb-1">Company Code Guidelines:</p>
                      <ul className="text-xs text-blue-700 space-y-1">
                        <li>• 3-10 characters long</li>
                        <li>• Uppercase letters and numbers only</li>
                        <li>• Should be memorable and related to your company</li>
                        <li>• Examples: "ACME", "TECH123", "GLOBALIT"</li>
                      </ul>
                    </div>
                  </div>



                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Description <span className="text-gray-400">(optional)</span>
                    </label>
                    <textarea
                      {...companyForm.register('description')}
                      rows={4}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="Brief description of your company..."
                    />
                    {companyForm.formState.errors.description && (
                      <p className="mt-1 text-sm text-red-600">{companyForm.formState.errors.description.message}</p>
                    )}
                    <p className="text-xs text-gray-500 mt-1">
                      Maximum 500 characters
                    </p>
                  </div>

                  <div className="flex justify-end">
                    <button
                      type="submit"
                      className="flex items-center gap-2 py-3 px-6 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors"
                    >
                      Next Step
                      <ArrowRight size={18} />
                    </button>
                  </div>
                </form>
              </motion.div>
            )}

            {/* Step 2: Admin Account */}
            {currentStep === 2 && (
              <motion.div
                key="step2"
                initial={{ opacity: 0, x: 50 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -50 }}
                className="space-y-6"
              >
                <form onSubmit={adminForm.handleSubmit(handleAdminSubmit)} className="space-y-6">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        First Name *
                      </label>
                      <input
                        {...adminForm.register('firstName')}
                        type="text"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="First Name"
                      />
                      {adminForm.formState.errors.firstName && (
                        <p className="mt-1 text-sm text-red-600">{adminForm.formState.errors.firstName.message}</p>
                      )}
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Last Name *
                      </label>
                      <input
                        {...adminForm.register('lastName')}
                        type="text"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="Last Name"
                      />
                      {adminForm.formState.errors.lastName && (
                        <p className="mt-1 text-sm text-red-600">{adminForm.formState.errors.lastName.message}</p>
                      )}
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Username *
                    </label>
                    <input
                      {...adminForm.register('username')}
                      type="text"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="admin_username"
                    />
                    {adminForm.formState.errors.username && (
                      <p className="mt-1 text-sm text-red-600">{adminForm.formState.errors.username.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Email Address *
                    </label>
                    <input
                      {...adminForm.register('email')}
                      type="email"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="admin@company.com"
                    />
                    {adminForm.formState.errors.email && (
                      <p className="mt-1 text-sm text-red-600">{adminForm.formState.errors.email.message}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Phone Number *
                    </label>
                    <input
                      {...adminForm.register('phoneNumber')}
                      type="tel"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="+1234567890"
                    />
                    {adminForm.formState.errors.phoneNumber && (
                      <p className="mt-1 text-sm text-red-600">{adminForm.formState.errors.phoneNumber.message}</p>
                    )}
                    <p className="text-xs text-gray-500 mt-1">
                      International format (e.g., +1234567890)
                    </p>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Password *
                    </label>
                    <div className="relative">
                      <input
                        {...adminForm.register('password')}
                        type={showPassword ? 'text' : 'password'}
                        className="w-full px-3 py-2 pr-10 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
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
                    {adminForm.formState.errors.password && (
                      <p className="mt-1 text-sm text-red-600">{adminForm.formState.errors.password.message}</p>
                    )}
                    <div className="mt-2 text-xs text-gray-500">
                      Must contain uppercase, lowercase, number and special character
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Confirm Password *
                    </label>
                    <div className="relative">
                      <input
                        {...adminForm.register('confirmPassword')}
                        type={showConfirmPassword ? 'text' : 'password'}
                        className="w-full px-3 py-2 pr-10 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
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
                    {adminForm.formState.errors.confirmPassword && (
                      <p className="mt-1 text-sm text-red-600">{adminForm.formState.errors.confirmPassword.message}</p>
                    )}
                  </div>

                  <div className="flex justify-between">
                    <button
                      type="button"
                      onClick={prevStep}
                      className="flex items-center gap-2 py-3 px-6 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition-colors"
                    >
                      <ArrowLeft size={18} />
                      Previous
                    </button>
                    
                    <button
                      type="submit"
                      disabled={isLoading}
                      className="flex items-center gap-2 py-3 px-6 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                      {isLoading ? (
                        <>
                          <Loader2 size={18} className="animate-spin" />
                          Creating Account...
                        </>
                      ) : (
                        <>
                          <CheckCircle size={18} />
                          Complete Registration
                        </>
                      )}
                    </button>
                  </div>
                </form>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            Already have an account?{' '}
            <Link to="/login" className="font-medium text-blue-600 hover:text-blue-500">
              Sign in here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default CompanyRegistration;