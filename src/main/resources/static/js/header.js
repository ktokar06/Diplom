document.addEventListener('DOMContentLoaded', function () {
    initializeChangePasswordModal();
});

function initializeChangePasswordModal() {
    const modal = document.getElementById('changePasswordModal');
    if (!modal) return;

    const form = modal.querySelector('form');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const errorDiv = document.getElementById('passwordError');

    if (form && newPasswordInput && confirmPasswordInput && errorDiv) {
        form.addEventListener('submit', function (e) {
            if (newPasswordInput.value !== confirmPasswordInput.value) {
                e.preventDefault();
                errorDiv.classList.remove('d-none');
            } else {
                errorDiv.classList.add('d-none');
            }
        });

        modal.addEventListener('show.bs.modal', function () {
            errorDiv.classList.add('d-none');
            if (newPasswordInput) newPasswordInput.value = '';
            if (confirmPasswordInput) confirmPasswordInput.value = '';
        });
    }
}