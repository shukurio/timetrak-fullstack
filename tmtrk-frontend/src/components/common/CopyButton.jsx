import { useState } from 'react';
import { Copy, Check } from 'lucide-react';
import toast from 'react-hot-toast';
import inviteService from '../../api/inviteService';

const CopyButton = ({ text, label = "Copy", successMessage = "Copied to clipboard!" }) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    try {
      await inviteService.copyToClipboard(text);
      setCopied(true);
      toast.success(successMessage);
      
      // Reset copied state after 2 seconds
      setTimeout(() => setCopied(false), 2000);
    } catch (error) {
      toast.error('Failed to copy to clipboard');
    }
  };

  return (
    <button
      onClick={handleCopy}
      className="flex items-center gap-1 px-2 py-1 text-sm text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded transition-colors"
      title={label}
    >
      {copied ? (
        <>
          <Check size={14} className="text-green-500" />
          <span className="text-green-500">Copied</span>
        </>
      ) : (
        <>
          <Copy size={14} />
          <span>{label}</span>
        </>
      )}
    </button>
  );
};

export default CopyButton;