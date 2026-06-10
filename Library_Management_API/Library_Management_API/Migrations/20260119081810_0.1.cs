using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Library_Management_API.Migrations
{
    /// <inheritdoc />
    public partial class _01 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "BookTable",
                columns: table => new
                {
                    book_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    isbn = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    title = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    description = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    author = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    category = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    total_copies = table.Column<int>(type: "int", nullable: false),
                    available_copies = table.Column<int>(type: "int", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_BookTable", x => x.book_id);
                });

            migrationBuilder.CreateTable(
                name: "FineTable",
                columns: table => new
                {
                    fine_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    borrow_id = table.Column<int>(type: "int", nullable: false),
                    overdue_days = table.Column<int>(type: "int", nullable: false),
                    fine_amount = table.Column<decimal>(type: "decimal(18,2)", nullable: false),
                    is_paid = table.Column<string>(type: "nvarchar(max)", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_FineTable", x => x.fine_id);
                });

            migrationBuilder.CreateTable(
                name: "MemberTable",
                columns: table => new
                {
                    member_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    membership_id = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    member_name = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    phone_number = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    email = table.Column<string>(type: "nvarchar(max)", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_MemberTable", x => x.member_id);
                });

            migrationBuilder.CreateTable(
                name: "Borrow_RecordTable",
                columns: table => new
                {
                    borrow_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    member_id = table.Column<int>(type: "int", nullable: false),
                    book_id = table.Column<int>(type: "int", nullable: false),
                    borrow_date = table.Column<DateOnly>(type: "date", nullable: false),
                    return_date = table.Column<DateOnly>(type: "date", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Borrow_RecordTable", x => x.borrow_id);
                    table.ForeignKey(
                        name: "FK_Borrow_RecordTable_BookTable_book_id",
                        column: x => x.book_id,
                        principalTable: "BookTable",
                        principalColumn: "book_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_Borrow_RecordTable_MemberTable_member_id",
                        column: x => x.member_id,
                        principalTable: "MemberTable",
                        principalColumn: "member_id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_Borrow_RecordTable_book_id",
                table: "Borrow_RecordTable",
                column: "book_id");

            migrationBuilder.CreateIndex(
                name: "IX_Borrow_RecordTable_member_id",
                table: "Borrow_RecordTable",
                column: "member_id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "Borrow_RecordTable");

            migrationBuilder.DropTable(
                name: "FineTable");

            migrationBuilder.DropTable(
                name: "BookTable");

            migrationBuilder.DropTable(
                name: "MemberTable");
        }
    }
}
