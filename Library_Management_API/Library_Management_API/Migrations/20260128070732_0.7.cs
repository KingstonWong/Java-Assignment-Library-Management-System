using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Library_Management_API.Migrations
{
    /// <inheritdoc />
    public partial class _07 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.RenameColumn(
                name: "borrow_date",
                table: "Borrow_RecordTable",
                newName: "request_date");

            migrationBuilder.AddColumn<DateOnly>(
                name: "approval_date",
                table: "Borrow_RecordTable",
                type: "date",
                nullable: true);

            migrationBuilder.AddColumn<DateOnly>(
                name: "due_date",
                table: "Borrow_RecordTable",
                type: "date",
                nullable: false,
                defaultValue: new DateOnly(1, 1, 1));

            migrationBuilder.AddColumn<bool>(
                name: "isWalkIn",
                table: "Borrow_RecordTable",
                type: "bit",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<DateOnly>(
                name: "pickup_date",
                table: "Borrow_RecordTable",
                type: "date",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "status",
                table: "Borrow_RecordTable",
                type: "nvarchar(max)",
                nullable: false,
                defaultValue: "");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "approval_date",
                table: "Borrow_RecordTable");

            migrationBuilder.DropColumn(
                name: "due_date",
                table: "Borrow_RecordTable");

            migrationBuilder.DropColumn(
                name: "isWalkIn",
                table: "Borrow_RecordTable");

            migrationBuilder.DropColumn(
                name: "pickup_date",
                table: "Borrow_RecordTable");

            migrationBuilder.DropColumn(
                name: "status",
                table: "Borrow_RecordTable");

            migrationBuilder.RenameColumn(
                name: "request_date",
                table: "Borrow_RecordTable",
                newName: "borrow_date");
        }
    }
}
