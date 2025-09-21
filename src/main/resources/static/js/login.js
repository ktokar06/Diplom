 document.addEventListener('DOMContentLoaded', function () {
        const groupSelect = document.getElementById('group');
        const userSelect = document.getElementById('username');
        const togglePassword = document.getElementById('togglePassword');
        const passwordInput = document.getElementById('password');

        // Функция для переключения видимости пароля
        if (togglePassword && passwordInput) {
            togglePassword.addEventListener('click', function () {
                const icon = this.querySelector('i');
                if (passwordInput.type === 'password') {
                    passwordInput.type = 'text';
                    icon.classList.remove('fa-eye');
                    icon.classList.add('fa-eye-slash');
                } else {
                    passwordInput.type = 'password';
                    icon.classList.remove('fa-eye-slash');
                    icon.classList.add('fa-eye');
                }
            });
        }

        // Функция обновления списка студентов
        function updateStudentList(students) {
            userSelect.innerHTML = '<option value="">Выберите студента</option>';

            if (students && students.length > 0) {
                students.forEach(function(student) {
                    var option = document.createElement('option');
                    var fullName = student.fullName || '';
                    var ticketNumber = student.studentTicketNumber || '';
                    option.value = ticketNumber;
                    option.textContent = fullName + ' (' + ticketNumber + ')';
                    userSelect.appendChild(option);
                });
            } else {
                userSelect.innerHTML = '<option value="">Студенты не найдены</option>';
            }
        }

        // Обработчик изменения выбора группы
        if (groupSelect) {
            groupSelect.addEventListener('change', function () {
                var selectedGroupId = this.value;

                if (selectedGroupId) {
                    // Преобразуем ID группы в число для сравнения
                    var groupId = parseInt(selectedGroupId);

                    // Ищем студентов для выбранной группы
                    if (studentsMap && studentsMap[groupId]) {
                        updateStudentList(studentsMap[groupId]);
                    } else {
                        userSelect.innerHTML = '<option value="">Студенты не найдены</option>';
                    }
                } else {
                    userSelect.innerHTML = '<option value="">Сначала выберите группу</option>';
                }
            });
        }
    });