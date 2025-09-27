document.addEventListener('DOMContentLoaded', function () {
    const groupSelect = document.getElementById('group');
    const userSelect = document.getElementById('username');
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');

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

    function updateStudentList(students) {
        userSelect.innerHTML = '<option value="" disabled selected>Выберите студента</option>';

        if (students && students.length > 0) {
            const sortedStudents = students.sort(function(a, b) {
                const nameA = (a.fullName || '').toLowerCase();
                const nameB = (b.fullName || '').toLowerCase();
                return nameA.localeCompare(nameB, 'ru');
            });

            sortedStudents.forEach(function(student) {
                var option = document.createElement('option');
                var fullName = student.fullName || '';
                var ticketNumber = student.studentTicketNumber || '';
                option.value = ticketNumber;
                option.textContent = fullName;
                userSelect.appendChild(option);
            });
        } else {
            userSelect.innerHTML = '<option value="">Студенты не найдены</option>';
        }
    }

    if (groupSelect) {
        groupSelect.addEventListener('change', function () {
            var selectedGroupId = this.value;

            if (selectedGroupId) {
                var groupId = parseInt(selectedGroupId);

                if (studentsMap && studentsMap[groupId]) {
                    updateStudentList(studentsMap[groupId]);
                } else {
                    userSelect.innerHTML = '<option value="">Студенты не найдены</option>';
                }
            } else {
                userSelect.innerHTML = '<option value="">Сначала выберите группу</option>';
            }
        });

        if (groupSelect.value) {
            var groupId = parseInt(groupSelect.value);
            if (studentsMap && studentsMap[groupId]) {
                updateStudentList(studentsMap[groupId]);
            }
        }
    }
});