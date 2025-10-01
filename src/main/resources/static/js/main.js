document.addEventListener('DOMContentLoaded', function() {
    const dropdownButtons = document.querySelectorAll('[id$="semesterDropdownButton"]');

    dropdownButtons.forEach(button => {
        const dropdownId = button.id.replace('Button', '');
        const dropdown = document.getElementById(dropdownId);

        if (dropdown) {
            button.addEventListener('click', function(e) {
                e.stopPropagation();
                dropdown.classList.toggle('hidden');
            });

            document.addEventListener('click', function() {
                dropdown.classList.add('hidden');
            });

            dropdown.addEventListener('click', function(e) {
                e.stopPropagation();
            });
        }
    });
});