import useAuthStore from '../../store/authStore';
import ProfileComponent from '../../components/common/ProfileComponent';

const AdminProfilePage = () => {
  const { user, updateUser } = useAuthStore();

  return (
    <ProfileComponent
      user={user}
      onUserUpdate={updateUser}
      isAdmin={true}
    />
  );
};

export default AdminProfilePage;