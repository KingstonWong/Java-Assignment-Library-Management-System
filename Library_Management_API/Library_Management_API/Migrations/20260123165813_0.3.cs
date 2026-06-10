using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Library_Management_API.Migrations
{
    /// <inheritdoc />
    public partial class _03 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "location",
                table: "BookTable",
                type: "nvarchar(max)",
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<int>(
                name: "publication_year",
                table: "BookTable",
                type: "int",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.AddColumn<string>(
                name: "publisher",
                table: "BookTable",
                type: "nvarchar(max)",
                nullable: false,
                defaultValue: "");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "location",
                table: "BookTable");

            migrationBuilder.DropColumn(
                name: "publication_year",
                table: "BookTable");

            migrationBuilder.DropColumn(
                name: "publisher",
                table: "BookTable");
        }
    }
}
