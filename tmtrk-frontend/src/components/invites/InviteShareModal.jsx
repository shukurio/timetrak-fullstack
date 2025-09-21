import { useState } from 'react';
import { Copy, Share2, Mail, MessageCircle, QrCode, Download, X, Check, ExternalLink } from 'lucide-react';
import toast from 'react-hot-toast';
import inviteService from '../../services/inviteService';

const InviteShareModal = ({ invite, companyName, isOpen, onClose }) => {
  const [copied, setCopied] = useState(false);
  const [qrCodeUrl, setQrCodeUrl] = useState(null);
  const [showQR, setShowQR] = useState(false);

  if (!isOpen || !invite) return null;

  const inviteUrl = inviteService.generateInviteUrl(invite.inviteCode);
  const whatsappUrl = inviteService.generateWhatsAppUrl(invite.inviteCode, companyName);
  const emailData = inviteService.generateEmailShare(invite.inviteCode, companyName);

  const handleCopy = async () => {
    try {
      await inviteService.copyToClipboard(inviteUrl);
      setCopied(true);
      toast.success('Invite link copied to clipboard!');
      setTimeout(() => setCopied(false), 2000);
    } catch (error) {
      toast.error('Failed to copy link');
    }
  };

  const handleWhatsApp = () => {
    window.open(whatsappUrl, '_blank');
  };

  const handleEmail = () => {
    const mailtoUrl = `mailto:?subject=${encodeURIComponent(emailData.subject)}&body=${encodeURIComponent(emailData.body)}`;
    window.location.href = mailtoUrl;
  };

  const handleGenerateQR = async () => {
    try {
      const qrUrl = await inviteService.generateQRCode(inviteUrl, 300);
      setQrCodeUrl(qrUrl);
      setShowQR(true);
    } catch (error) {
      toast.error('Failed to generate QR code');
    }
  };

  const handleDownloadQR = () => {
    if (qrCodeUrl) {
      const link = document.createElement('a');
      link.href = qrCodeUrl;
      link.download = `invite-${invite.inviteCode}-qr.png`;
      link.click();
    }
  };

  const handleDirectShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: `Join ${companyName}`,
          text: `You're invited to join ${companyName}!`,
          url: inviteUrl,
        });
      } catch (error) {
        if (error.name !== 'AbortError') {
          toast.error('Sharing failed');
        }
      }
    } else {
      handleCopy();
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b">
          <div>
            <h3 className="text-lg font-semibold text-gray-900">Share Invite</h3>
            <p className="text-sm text-gray-500 mt-1">
              {invite.description || `Code: ${invite.inviteCode}`}
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        {/* Invite Info */}
        <div className="p-6 bg-blue-50 border-b">
          <div className="text-sm space-y-1">
            <div className="flex justify-between">
              <span className="text-gray-600">Usage:</span>
              <span className="font-medium">{invite.currentUses || 0}/{invite.maxUses}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Department:</span>
              <span className="font-medium">{invite.departmentName || 'Any'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Expires:</span>
              <span className="font-medium">
                {new Date(invite.expiresAt).toLocaleDateString()}
              </span>
            </div>
          </div>

          {/* Progress Bar */}
          <div className="mt-3">
            <div className="flex justify-between text-xs text-gray-600 mb-1">
              <span>Progress</span>
              <span>{inviteService.getUsagePercentage(invite)}%</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="bg-blue-600 h-2 rounded-full transition-all"
                style={{ width: `${inviteService.getUsagePercentage(invite)}%` }}
              />
            </div>
          </div>
        </div>

        {/* Share URL */}
        <div className="p-6 border-b">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Invite Link
          </label>
          <div className="flex items-center gap-2">
            <input
              type="text"
              value={inviteUrl}
              readOnly
              className="flex-1 px-3 py-2 bg-gray-50 border border-gray-300 rounded-md text-sm font-mono"
            />
            <button
              onClick={handleCopy}
              className="flex items-center gap-1 px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors text-sm"
            >
              {copied ? (
                <>
                  <Check size={16} />
                  Copied
                </>
              ) : (
                <>
                  <Copy size={16} />
                  Copy
                </>
              )}
            </button>
          </div>
        </div>

        {/* Sharing Options */}
        <div className="p-6">
          <h4 className="text-sm font-medium text-gray-700 mb-4">Share Options</h4>
          
          <div className="grid grid-cols-2 gap-3">
            {/* WhatsApp */}
            <button
              onClick={handleWhatsApp}
              className="flex items-center gap-3 p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <div className="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center">
                <MessageCircle size={16} className="text-white" />
              </div>
              <span className="text-sm font-medium text-gray-700">WhatsApp</span>
            </button>

            {/* Email */}
            <button
              onClick={handleEmail}
              className="flex items-center gap-3 p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                <Mail size={16} className="text-white" />
              </div>
              <span className="text-sm font-medium text-gray-700">Email</span>
            </button>

            {/* QR Code */}
            <button
              onClick={handleGenerateQR}
              className="flex items-center gap-3 p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <div className="w-8 h-8 bg-purple-500 rounded-full flex items-center justify-center">
                <QrCode size={16} className="text-white" />
              </div>
              <span className="text-sm font-medium text-gray-700">QR Code</span>
            </button>

            {/* Direct Share */}
            <button
              onClick={handleDirectShare}
              className="flex items-center gap-3 p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <div className="w-8 h-8 bg-orange-500 rounded-full flex items-center justify-center">
                <Share2 size={16} className="text-white" />
              </div>
              <span className="text-sm font-medium text-gray-700">More</span>
            </button>
          </div>

          {/* QR Code Display */}
          {showQR && qrCodeUrl && (
            <div className="mt-6 p-4 bg-gray-50 rounded-lg text-center">
              <h5 className="text-sm font-medium text-gray-700 mb-3">QR Code</h5>
              <div className="inline-block p-4 bg-white rounded-lg shadow-sm">
                <img
                  src={qrCodeUrl}
                  alt="Invite QR Code"
                  className="w-48 h-48 mx-auto"
                />
              </div>
              <div className="mt-3 flex justify-center gap-2">
                <button
                  onClick={handleDownloadQR}
                  className="flex items-center gap-1 px-3 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 transition-colors text-sm"
                >
                  <Download size={16} />
                  Download
                </button>
                <button
                  onClick={() => window.open(inviteUrl, '_blank')}
                  className="flex items-center gap-1 px-3 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors text-sm"
                >
                  <ExternalLink size={16} />
                  Test Link
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-6 bg-gray-50 rounded-b-xl">
          <p className="text-xs text-gray-500 text-center">
            Share this link with new employees to join {companyName}
          </p>
        </div>
      </div>
    </div>
  );
};

export default InviteShareModal;