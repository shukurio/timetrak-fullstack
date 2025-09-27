import React from 'react';

const AddButton = ({
  onClick,
  children,
  className = "",
  disabled = false,
  icon: Icon
}) => {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`btn-primary flex items-center justify-center ${className}`}
    >
      {Icon && <Icon className="h-4 w-4 mr-2" />}
      {children}
    </button>
  );
};

export default AddButton;