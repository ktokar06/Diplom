document.addEventListener('DOMContentLoaded', function () {
    const groupSelect = document.getElementById('group');
    const userSelect = document.getElementById('username');

    // Функция обновления списка пользователей
    function updateUserList(users, isTeacher) {
        userSelect.innerHTML = '<option value="">Выберите ' + (isTeacher ? 'преподавателя' : 'студента') + '</option>';

        if (users && users.length > 0) {
            for (var i = 0; i < users.length; i++) {
                var user = users[i];
                var option = document.createElement('option');
                if (isTeacher) {
                    var fullName = user.fullName || '';
                    option.value = fullName;
                    option.textContent = fullName;
                } else {
                    var fullName = user.fullName || '';
                    var ticketNumber = user.studentTicketNumber || '';
                    option.value = ticketNumber;
                    option.textContent = fullName;
                }
                if (option.value) {
                    userSelect.appendChild(option);
                }
            }
        }
    }

    // Обработчик события изменения выбора группы
    groupSelect.addEventListener('change', function () {
        var selectedGroupId = this.value;

        if (selectedGroupId === 'teachers') {
            if (window.teachers && Array.isArray(window.teachers)) {
                updateUserList(window.teachers, true);
            } else {
                userSelect.innerHTML = '<option value="">Преподаватели не найдены</option>';
            }
        } else if (selectedGroupId) {
            var groupId = parseInt(selectedGroupId);
            if (window.studentsMap && window.studentsMap[groupId]) {
                updateUserList(window.studentsMap[groupId], false);
            } else {
                userSelect.innerHTML = '<option value="">Студенты не найдены</option>';
            }
        } else {
            userSelect.innerHTML = '<option value="">Сначала выберите группу</option>';
        }
    });

    // Функция переключения видимости пароля
    window.togglePassword = function() {
        var passwordInput = document.getElementById('password');
        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
        } else {
            passwordInput.type = 'password';
        }
    };
});