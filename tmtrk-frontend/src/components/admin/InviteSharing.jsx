import { useState } from 'react';
import { Copy, QrCode, Share2, Check, ExternalLink } from 'lucide-react';
import toast from 'react-hot-toast';

const InviteSharing = ({ inviteCode, onQRCodeGenerate }) => {
  const [copied, setCopied] = useState(false);
  const inviteUrl = `${window.location.origin}/register?invite=${inviteCode}`;

  const copyToClipboard = async () => {
    try {
      await navigator.clipboard.writeText(inviteUrl);
      setCopied(true);
      toast.success('Invite link copied to clipboard!');
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      toast.error('Failed to copy link');
    }
  };

  const openInNewTab = () => {
    window.open(inviteUrl, '_blank');
  };

  const shareViaWeb = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: 'Join Our Team',
          text: 'You have been invited to join our team!',
          url: inviteUrl,
        });
      } catch (err) {
        if (err.name !== 'AbortError') {
          toast.error('Sharing failed');
        }
      }
    } else {
      copyToClipboard();
    }
  };

  return (
    <div className="bg-gray-50 rounded-lg p-4 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-medium text-gray-700">Share Invite Link</h3>
        <div className="flex items-center gap-2">
          <button
            onClick={openInNewTab}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-white rounded-lg transition-colors"
            title="Open in new tab"
          >
            <ExternalLink size={16} />
          </button>
          <button
            onClick={() => onQRCodeGenerate?.(inviteUrl)}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-white rounded-lg transition-colors"
            title="Generate QR Code"
          >
            <QrCode size={16} />
          </button>
        </div>
      </div>

      <div className="flex items-center gap-2">
        <input
          type="text"
          value={inviteUrl}
          readOnly
          className="flex-1 px-3 py-2 text-sm bg-white border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
        <button
          onClick={copyToClipboard}
          className="flex items-center gap-2 px-3 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors"
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

      <div className="flex gap-2">
        <button
          onClick={shareViaWeb}
          className="flex items-center gap-2 px-3 py-2 text-sm text-gray-600 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors"
        >
          <Share2 size={16} />
          Share
        </button>
      </div>

      <div className="text-xs text-gray-500">
        <p>Share this link with new employees to join your team.</p>
        <p className="font-mono bg-gray-100 px-2 py-1 rounded mt-1">
          Invite Code: {inviteCode}
        </p>
      </div>
    </div>
  );
};

export default InviteSharing;