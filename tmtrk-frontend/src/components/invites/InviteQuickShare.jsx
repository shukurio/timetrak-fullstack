import { useState } from 'react';
import { Copy, Share2, Check, ChevronDown } from 'lucide-react';
import toast from 'react-hot-toast';
import inviteService from '../../api/inviteService';

const InviteQuickShare = ({ invite, companyName = 'our company' }) => {
  const [copied, setCopied] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);

  const inviteUrl = inviteService.generateInviteUrl(invite.inviteCode);

  const handleCopy = async (e) => {
    e.stopPropagation();
    try {
      await inviteService.copyToClipboard(inviteUrl);
      setCopied(true);
      toast.success('Link copied!');
      setTimeout(() => setCopied(false), 2000);
    } catch (error) {
      toast.error('Failed to copy');
    }
  };

  const handleQuickShare = async (e) => {
    e.stopPropagation();
    if (navigator.share) {
      try {
        await navigator.share({
          title: `Join ${companyName}`,
          url: inviteUrl,
        });
      } catch (error) {
        if (error.name !== 'AbortError') {
          handleCopy(e);
        }
      }
    } else {
      setShowDropdown(!showDropdown);
    }
  };

  const handleWhatsApp = (e) => {
    e.stopPropagation();
    const whatsappUrl = inviteService.generateWhatsAppUrl(invite.inviteCode, companyName);
    window.open(whatsappUrl, '_blank');
    setShowDropdown(false);
  };

  const handleEmail = (e) => {
    e.stopPropagation();
    const emailData = inviteService.generateEmailShare(invite.inviteCode, companyName);
    const mailtoUrl = `mailto:?subject=${encodeURIComponent(emailData.subject)}&body=${encodeURIComponent(emailData.body)}`;
    window.location.href = mailtoUrl;
    setShowDropdown(false);
  };

  return (
    <div className="relative inline-flex">
      <div className="flex items-center">
        <button
          onClick={handleCopy}
          className="flex items-center gap-1 px-2 py-1 text-sm text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-l-md transition-colors border-r border-gray-200"
          title="Copy link"
        >
          {copied ? (
            <Check size={14} className="text-green-500" />
          ) : (
            <Copy size={14} />
          )}
        </button>
        
        <button
          onClick={handleQuickShare}
          className="flex items-center gap-1 px-2 py-1 text-sm text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-r-md transition-colors"
          title="Share"
        >
          <Share2 size={14} />
          {!navigator.share && <ChevronDown size={12} />}
        </button>
      </div>

      {/* Dropdown for browsers without native sharing */}
      {showDropdown && !navigator.share && (
        <div className="absolute top-full right-0 mt-1 w-32 bg-white border border-gray-200 rounded-md shadow-lg z-10">
          <button
            onClick={handleWhatsApp}
            className="block w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            WhatsApp
          </button>
          <button
            onClick={handleEmail}
            className="block w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            Email
          </button>
        </div>
      )}

      {/* Backdrop to close dropdown */}
      {showDropdown && (
        <div
          className="fixed inset-0 z-5"
          onClick={() => setShowDropdown(false)}
        />
      )}
    </div>
  );
};

export default InviteQuickShare;